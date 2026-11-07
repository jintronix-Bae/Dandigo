package Dandigo.SOS_map.domain.complaint.dto.response;

import java.util.List;

// 6번 (complaints는 최신순 최대 200개, totalCount는 지도 범위 안 실제 전체 개수)
public record NearbyComplaintsResponse(long totalCount, List<ComplaintSummaryResponse> complaints) {
}
