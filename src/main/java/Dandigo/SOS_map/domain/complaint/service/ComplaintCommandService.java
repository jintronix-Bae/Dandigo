package Dandigo.SOS_map.domain.complaint.service;

import Dandigo.SOS_map.domain.complaint.dto.request.ComplaintCreateRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.ComplaintPreviewRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.ComplaintUpdateRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.PrivacyCheckRequest;
import Dandigo.SOS_map.domain.complaint.dto.request.ResultImageRequest;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintDetailResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintPreviewResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ResultImageResponse;
import Dandigo.SOS_map.domain.complaint.entity.Complaint;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import Dandigo.SOS_map.domain.complaint.repository.ComplaintRepository;
import Dandigo.SOS_map.domain.festival.entity.Festival;
import Dandigo.SOS_map.domain.festival.repository.FestivalRepository;
import Dandigo.SOS_map.domain.image.entity.Image;
import Dandigo.SOS_map.domain.image.entity.ImageType;
import Dandigo.SOS_map.domain.image.service.ImageService;
import Dandigo.SOS_map.domain.user.repository.MemberRepository;
import Dandigo.SOS_map.global.common.DateTimes;
import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import Dandigo.SOS_map.global.infra.gemini.GeminiImageClient;
import Dandigo.SOS_map.global.infra.image.ImageProcessor;
import Dandigo.SOS_map.global.infra.image.RelativeBox;
import Dandigo.SOS_map.global.infra.rekognition.FaceDetector;
import Dandigo.SOS_map.global.infra.s3.S3FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 민원 등록 흐름(9 → 10 → 11번)과 수정(17번)·삭제(18번)
@Service
@RequiredArgsConstructor
public class ComplaintCommandService {

    private final ComplaintRepository complaintRepository;
    private final FestivalRepository festivalRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final ComplaintQueryService complaintQueryService;
    private final AreaResolver areaResolver;
    private final S3FileStorage s3FileStorage;
    private final FaceDetector faceDetector;
    private final ImageProcessor imageProcessor;
    private final GeminiImageClient geminiImageClient;

    // #1.민원 등록 점검 (9번): 얼굴 블러 사진을 새로 만들고 담당 부서·지역을 계산한다. 민원은 저장하지 않는다.
    // 외부 호출(Rekognition)이 길어질 수 있어 메서드 전체를 트랜잭션으로 묶지 않는다.
    public ComplaintPreviewResponse preview(Long userId, ComplaintPreviewRequest request) {
        Image original = imageService.getOwnedImage(userId, request.imageId(), ImageType.ORIGINAL);

        byte[] originalBytes = s3FileStorage.download(original.getUrl());
        List<RelativeBox> faces = faceDetector.detectFaces(originalBytes);
        // 얼굴이 없으면 원본과 같은 사진을 블러 사진으로 저장한다 (11번에는 항상 블러 사진 ID를 보내도록)
        byte[] blurredBytes = faces.isEmpty()
                ? originalBytes
                : imageProcessor.mosaic(originalBytes, original.getContentType(), faces);
        Image blurred = imageService.store(userId, original, ImageType.BLURRED, blurredBytes, original.getContentType());

        ComplaintCategory category = request.category();
        return new ComplaintPreviewResponse(
                category.getAgency(),
                category.getEstimatedPeriod(),
                areaResolver.resolve(request.latitude(), request.longitude(), request.address()),
                new ComplaintPreviewResponse.PreviewImage(
                        original.getId(), blurred.getId(), blurred.getUrl(), faces.size()));
    }

    // #2.민원 처리 결과 미리보기 (10번): 블러 사진을 바탕으로 Gemini가 "해결된 모습"을 그린다.
    public ResultImageResponse createResultImage(Long userId, ResultImageRequest request) {
        Image blurred = imageService.getOwnedImage(userId, request.blurredImageId(), ImageType.BLURRED);

        byte[] blurredBytes = s3FileStorage.download(blurred.getUrl());
        GeminiImageClient.GeneratedImage generated = geminiImageClient.editImage(
                buildResultPrompt(request), blurredBytes, blurred.getContentType());

        String contentType = imageProcessor.detectContentType(generated.bytes());
        if (contentType == null) {
            throw new CustomException(ErrorCode.AI_UNAVAILABLE);
        }
        Image result = imageService.store(userId, blurred, ImageType.RESULT, generated.bytes(), contentType);
        return new ResultImageResponse(result.getId(), result.getUrl(), request.category().getResultDescription());
    }

    // #3.민원 등록 (11번)
    @Transactional
    public ComplaintDetailResponse create(Long userId, ComplaintCreateRequest request) {
        Image image = getAttachableImage(userId, request.imageId(), ImageType.BLURRED);
        Image resultImage = request.resultImageId() == null
                ? null
                : getAttachableImage(userId, request.resultImageId(), ImageType.RESULT);
        Festival festival = findActiveFestival(request.festivalId());
        validateFestivalCategory(request.category(), festival);

        PrivacyCheckRequest privacy = request.privacyCheck();
        Complaint complaint = Complaint.builder()
                .user(memberRepository.getReferenceById(userId))
                .image(image)
                .resultImage(resultImage)
                .festival(festival)
                .category(request.category())
                .title(request.title().trim())
                .description(request.description().trim())
                .address(request.address().trim())
                .area(areaResolver.resolve(request.latitude(), request.longitude(), request.address()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .faceBlurConfirmed(privacy.faceBlurConfirmed())
                .containsAnimalCarcass(privacy.containsAnimalCarcass())
                .animalBlurConfirmed(privacy.animalBlurConfirmed())
                .build();

        image.attach();
        if (resultImage != null) {
            resultImage.attach();
        }
        complaintRepository.save(complaint);
        removeOriginalOf(image);
        return complaintQueryService.toDetail(userId, complaint);
    }

    // #4.민원 수정 (17번): 보낸 항목만 바꾼다.
    @Transactional
    public ComplaintDetailResponse update(Long userId, Long complaintId, ComplaintUpdateRequest request) {
        Complaint complaint = getMyModifiableComplaint(userId, complaintId, ErrorCode.COMPLAINT_NOT_EDITABLE);

        if (request.title() != null) {
            complaint.changeTitle(requireText(request.title()));
        }
        if (request.description() != null) {
            complaint.changeDescription(requireText(request.description()));
        }
        // 위치는 주소·위도·경도를 함께 보내야 한다
        if (request.address() != null || request.latitude() != null || request.longitude() != null) {
            if (request.address() == null || request.latitude() == null || request.longitude() == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            String address = requireText(request.address());
            complaint.changeLocation(address,
                    areaResolver.resolve(request.latitude(), request.longitude(), address),
                    request.latitude(), request.longitude());
        }
        if (request.category() != null) {
            complaint.changeCategory(request.category());
            if (request.category() != ComplaintCategory.FESTIVAL && request.festivalId() == null) {
                complaint.changeFestival(null); // 축제 민원이 아니게 바뀌면 축제 연결을 끊는다
            }
        }
        // 축제를 새로 바꿀 때만 진행 중인지 확인한다 (같은 축제 번호를 다시 보내도 에러 없음)
        if (request.festivalId() != null && !request.festivalId().equals(currentFestivalId(complaint))) {
            complaint.changeFestival(findActiveFestival(request.festivalId()));
        }
        validateFestivalCategory(complaint.getCategory(), complaint.getFestival());

        if (request.privacyCheck() != null) {
            PrivacyCheckRequest privacy = request.privacyCheck();
            complaint.changePrivacyCheck(
                    privacy.faceBlurConfirmed(), privacy.containsAnimalCarcass(), privacy.animalBlurConfirmed());
        }
        if (request.imageId() != null && !request.imageId().equals(currentImageId(complaint))) {
            replaceImage(userId, complaint, request);
        }
        return complaintQueryService.toDetail(userId, complaint);
    }

    // #5.민원 삭제 (18번): 민원은 숨김 삭제(deleted_at 기록), 사진 파일은 S3·DB에서 지운다
    @Transactional
    public void delete(Long userId, Long complaintId) {
        Complaint complaint = getMyModifiableComplaint(userId, complaintId, ErrorCode.COMPLAINT_NOT_DELETABLE);
        Image image = complaint.getImage();
        Image resultImage = complaint.getResultImage();

        complaint.changeImage(null);
        complaint.changeResultImage(null);
        complaint.softDelete();
        if (image != null) {
            imageService.remove(image);
        }
        if (resultImage != null) {
            imageService.remove(resultImage);
        }
    }

    // 사진 교체: 새 블러 사진을 연결하고, 이전 사진과 그 사진으로 만든 AI 결과 이미지는 서버가 지운다.
    // 사진을 바꿀 때는 privacyCheck 필수
    private void replaceImage(Long userId, Complaint complaint, ComplaintUpdateRequest request) {
        if (request.privacyCheck() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        Image newImage = getAttachableImage(userId, request.imageId(), ImageType.BLURRED);
        Image oldImage = complaint.getImage();
        Image oldResultImage = complaint.getResultImage();

        complaint.changeImage(newImage);
        complaint.changeResultImage(null);
        newImage.attach();
        if (oldImage != null) {
            imageService.remove(oldImage);
        }
        if (oldResultImage != null) {
            imageService.remove(oldResultImage);
        }
        removeOriginalOf(newImage);
    }

    // 얼굴이 그대로 있는 원본 사진은 민원에 연결된 뒤 바로 지운다 (공개 버킷에 남지 않도록)
    private void removeOriginalOf(Image blurredImage) {
        Image original = blurredImage.getSourceImage();
        if (original != null && original.getType() == ImageType.ORIGINAL) {
            imageService.remove(original);
        }
    }

    private Complaint getMyModifiableComplaint(Long userId, Long complaintId, ErrorCode notModifiable) {
        Complaint complaint = complaintRepository.findDetailById(complaintId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLAINT_NOT_FOUND));
        if (!complaint.isWrittenBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (!complaint.isModifiable()) {
            throw new CustomException(notModifiable);
        }
        return complaint;
    }

    // 내 사진이고, 아직 다른 민원에 쓰이지 않은 사진만 연결할 수 있다
    private Image getAttachableImage(Long userId, Long imageId, ImageType type) {
        Image image = imageService.getOwnedImage(userId, imageId, type);
        if (image.isAttached()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        return image;
    }

    private Festival findActiveFestival(Long festivalId) {
        if (festivalId == null) {
            return null;
        }
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
        if (!festival.isActiveOn(DateTimes.today())) {
            throw new CustomException(ErrorCode.FESTIVAL_NOT_ACTIVE);
        }
        return festival;
    }

    // 카테고리가 FESTIVAL이면 축제 번호 필수
    private void validateFestivalCategory(ComplaintCategory category, Festival festival) {
        if (category == ComplaintCategory.FESTIVAL && festival == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private Long currentFestivalId(Complaint complaint) {
        return complaint.getFestival() == null ? null : complaint.getFestival().getId();
    }

    private Long currentImageId(Complaint complaint) {
        return complaint.getImage() == null ? null : complaint.getImage().getId();
    }

    private String requireText(String value) {
        if (value.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        return value.trim();
    }

    private String buildResultPrompt(ResultImageRequest request) {
        ComplaintCategory category = request.category();
        return """
                This photo was submitted as a civic complaint in Daegu, South Korea.
                Complaint category: %s (%s)
                Complaint title: %s
                Complaint details: %s

                Edit this photo to show how the same scene would look AFTER the problem has been fixed by the city.
                %s
                Keep the same camera angle, location, lighting and every unrelated object unchanged.
                Keep any blurred or pixelated faces blurred. Do not add any people, text, logos or watermarks.
                """.formatted(category.name(), category.getDisplayName(),
                request.title().trim(), request.description().trim(), category.getFixInstruction());
    }
}
