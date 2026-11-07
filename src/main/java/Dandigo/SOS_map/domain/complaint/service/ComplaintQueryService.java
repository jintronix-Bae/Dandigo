package Dandigo.SOS_map.domain.complaint.service;

import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintDetailResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintPageResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintSummaryResponse;
import Dandigo.SOS_map.domain.complaint.dto.response.NearbyComplaintsResponse;
import Dandigo.SOS_map.domain.complaint.entity.Complaint;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintStatus;
import Dandigo.SOS_map.domain.complaint.repository.ComplaintEmpathyRepository;
import Dandigo.SOS_map.domain.complaint.repository.ComplaintRepository;
import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 민원 조회: 6번 지도 주변, 7번 목록, 8번 상세
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintQueryService {

    public static final String SORT_LATEST = "latest";
    public static final String SORT_EMPATHY = "empathy";
    private static final int NEARBY_LIMIT = 200;

    private final ComplaintRepository complaintRepository;
    private final ComplaintEmpathyRepository empathyRepository;

    // #1.지도 범위 안의 민원 (6번)
    public NearbyComplaintsResponse findNearby(Long userId, double minLat, double maxLat,
                                               double minLng, double maxLng, ComplaintCategory category) {
        if (minLat > maxLat || minLng > maxLng) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        BigDecimal latFrom = BigDecimal.valueOf(minLat);
        BigDecimal latTo = BigDecimal.valueOf(maxLat);
        BigDecimal lngFrom = BigDecimal.valueOf(minLng);
        BigDecimal lngTo = BigDecimal.valueOf(maxLng);

        // 지도를 크게 축소해도 최신 200개까지만 보낸다. totalCount는 범위 안 실제 전체 개수
        List<Complaint> complaints = complaintRepository.findInBounds(
                latFrom, latTo, lngFrom, lngTo, category, PageRequest.of(0, NEARBY_LIMIT));
        long totalCount = complaintRepository.countInBounds(latFrom, latTo, lngFrom, lngTo, category);
        return new NearbyComplaintsResponse(totalCount, toSummaries(userId, complaints));
    }

    // #2.전체 목록 (7번)
    public ComplaintPageResponse search(Long userId, String area, ComplaintCategory category, Long festivalId,
                                        ComplaintStatus status, String sort, int page, int size) {
        Sort order = switch (sort) {
            case SORT_LATEST -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case SORT_EMPATHY -> Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id"));
            default -> throw new CustomException(ErrorCode.INVALID_INPUT);
        };
        Page<Complaint> result = complaintRepository.search(
                emptyToNull(area), category, festivalId, status, PageRequest.of(page, size, order));
        return new ComplaintPageResponse(
                toSummaries(userId, result.getContent()),
                page,
                size,
                result.getTotalElements(),
                result.hasNext());
    }

    // #3.상세 (8번)
    public ComplaintDetailResponse getDetail(Long userId, Long complaintId) {
        Complaint complaint = complaintRepository.findDetailById(complaintId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLAINT_NOT_FOUND));
        return toDetail(userId, complaint);
    }

    public ComplaintDetailResponse toDetail(Long userId, Complaint complaint) {
        boolean liked = empathyRepository.existsByComplaintIdAndUserId(complaint.getId(), userId);
        return ComplaintDetailResponse.of(complaint, liked, complaint.isWrittenBy(userId));
    }

    // 목록의 liked 값을 한 번의 쿼리로 채운다
    public List<ComplaintSummaryResponse> toSummaries(Long userId, List<Complaint> complaints) {
        if (complaints.isEmpty()) {
            return List.of();
        }
        Set<Long> likedIds = new HashSet<>(empathyRepository.findLikedComplaintIds(
                userId, complaints.stream().map(Complaint::getId).toList()));
        return complaints.stream()
                .map(c -> ComplaintSummaryResponse.of(c, likedIds.contains(c.getId())))
                .toList();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
