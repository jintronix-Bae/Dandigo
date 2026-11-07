package Dandigo.SOS_map.domain.complaint.dto.response;

import Dandigo.SOS_map.domain.complaint.entity.Complaint;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintStatus;
import Dandigo.SOS_map.global.common.DateTimes;

import java.time.OffsetDateTime;

// 공통 규칙의 "민원 요약 객체" (5·6·7번에서 같은 모양으로 사용)
public record ComplaintSummaryResponse(
        Long complaintId,
        String title,
        String address,
        String area,
        ComplaintCategory category,
        ComplaintStatus status,
        double latitude,
        double longitude,
        int likeCount,
        boolean liked,
        FestivalTag festival,
        String thumbnailUrl,
        OffsetDateTime createdAt
) {
    public static ComplaintSummaryResponse of(Complaint complaint, boolean liked) {
        return new ComplaintSummaryResponse(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getAddress(),
                complaint.getArea(),
                complaint.getCategory(),
                complaint.getStatus(),
                complaint.getLatitude().doubleValue(),
                complaint.getLongitude().doubleValue(),
                complaint.getLikeCount(),
                liked,
                FestivalTag.from(complaint.getFestival()),
                complaint.getImage() == null ? null : complaint.getImage().getThumbnailUrl(),
                DateTimes.toOffset(complaint.getCreatedAt()));
    }
}
