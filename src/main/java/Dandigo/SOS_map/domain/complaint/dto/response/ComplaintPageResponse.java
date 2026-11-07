package Dandigo.SOS_map.domain.complaint.dto.response;

import java.util.List;

// 7번
public record ComplaintPageResponse(
        List<ComplaintSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        boolean hasNext
) {
}
