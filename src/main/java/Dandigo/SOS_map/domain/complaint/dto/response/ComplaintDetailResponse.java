package Dandigo.SOS_map.domain.complaint.dto.response;

import Dandigo.SOS_map.domain.complaint.entity.Complaint;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintStatus;
import Dandigo.SOS_map.global.common.DateTimes;

import java.time.OffsetDateTime;

// 8번 상세 (11번 등록, 17번 수정 응답도 같은 모양)
public record ComplaintDetailResponse(
        Long complaintId,
        String title,
        String description,
        String address,
        String area,
        ComplaintCategory category,
        ComplaintStatus status,
        double latitude,
        double longitude,
        String agency,
        String estimatedPeriod,
        String imageUrl,
        String resultImageUrl,
        FestivalTag festival,
        int likeCount,
        boolean liked,
        boolean mine,
        OffsetDateTime createdAt
) {
    public static ComplaintDetailResponse of(Complaint complaint, boolean liked, boolean mine) {
        return new ComplaintDetailResponse(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getAddress(),
                complaint.getArea(),
                complaint.getCategory(),
                complaint.getStatus(),
                complaint.getLatitude().doubleValue(),
                complaint.getLongitude().doubleValue(),
                complaint.getCategory().getAgency(),
                complaint.getCategory().getEstimatedPeriod(),
                complaint.getImage() == null ? null : complaint.getImage().getUrl(),
                complaint.getResultImage() == null ? null : complaint.getResultImage().getUrl(),
                FestivalTag.from(complaint.getFestival()),
                complaint.getLikeCount(),
                liked,
                mine,
                DateTimes.toOffset(complaint.getCreatedAt()));
    }
}
