package Dandigo.SOS_map.domain.image.service;

import Dandigo.SOS_map.domain.image.dto.response.ImageUploadResponse;
import Dandigo.SOS_map.domain.image.entity.Image;
import Dandigo.SOS_map.domain.image.entity.ImageType;
import Dandigo.SOS_map.domain.image.repository.ImageRepository;
import Dandigo.SOS_map.domain.user.repository.MemberRepository;
import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import Dandigo.SOS_map.global.infra.image.ImageProcessor;
import Dandigo.SOS_map.global.infra.s3.S3FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// 사진 업로드(14번)·삭제(15번)와, 블러/결과 이미지 저장 공통 처리
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final S3FileStorage s3FileStorage;
    private final ImageProcessor imageProcessor;

    // #1.사진 업로드 (14번)
    public ImageUploadResponse upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        byte[] bytes = readBytes(file);

        // 확장자나 Content-Type 헤더가 아니라 실제 파일 내용으로 jpg/png 여부를 판별한다 (HEIC 등은 415)
        String contentType = imageProcessor.detectContentType(bytes);
        if (contentType == null) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
        imageProcessor.read(bytes); // 깨진 파일이면 415
        bytes = imageProcessor.normalizeOrientation(bytes, contentType); // 휴대폰 사진 회전 바로잡기

        Image saved = store(userId, null, ImageType.ORIGINAL, bytes, contentType);
        return new ImageUploadResponse(saved.getId(), saved.getUrl());
    }

    // #2.사진 삭제 (15번)
    @Transactional
    public void delete(Long userId, Long imageId) {
        Image image = getOwnedImage(userId, imageId, null);
        if (image.isAttached()) {
            throw new CustomException(ErrorCode.IMAGE_IN_USE);
        }
        remove(image);
    }

    // #3.내 사진인지, 기대한 종류(원본/블러/결과)인지 확인 후 반환
    @Transactional(readOnly = true)
    public Image getOwnedImage(Long userId, Long imageId, ImageType expectedType) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
        if (!image.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (expectedType != null && image.getType() != expectedType) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        return image;
    }

    // #4.S3에 원본+썸네일을 올리고 IMAGES 행을 만든다 (상태는 TEMP)
    @Transactional
    public Image store(Long uploaderId, Image source, ImageType type, byte[] bytes, String contentType) {
        String fileName = UUID.randomUUID() + "." + ImageProcessor.extensionOf(contentType);
        String url = s3FileStorage.upload(
                "images/" + type.name().toLowerCase(Locale.ROOT) + "/" + fileName, bytes, contentType);
        String thumbnailUrl = s3FileStorage.upload(
                "thumbnails/" + fileName, imageProcessor.thumbnail(bytes, contentType), contentType);

        Image image = Image.create(memberRepository.getReferenceById(uploaderId), source, type,
                url, thumbnailUrl, contentType, bytes.length);
        return imageRepository.save(image);
    }

    // #5.DB 행 삭제 + (커밋 후) S3 파일 삭제
    @Transactional
    public void remove(Image image) {
        // 이 사진에서 만들어진 사진들의 source 연결을 먼저 끊는다 (FK 위반 방지)
        imageRepository.findAllBySourceImageId(image.getId()).forEach(Image::clearSource);
        imageRepository.delete(image);
        deleteFilesAfterCommit(List.of(image.getUrl(), image.getThumbnailUrl()));
    }

    // DB 작업이 롤백되면 S3 파일은 남겨둔다
    private void deleteFilesAfterCommit(List<String> urls) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    urls.forEach(s3FileStorage::delete);
                }
            });
        } else {
            urls.forEach(s3FileStorage::delete);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
