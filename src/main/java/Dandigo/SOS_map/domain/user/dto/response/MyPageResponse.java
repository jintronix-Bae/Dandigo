package Dandigo.SOS_map.domain.user.dto.response;

import Dandigo.SOS_map.domain.complaint.dto.response.ComplaintSummaryResponse;

import java.util.List;

// 5번
public record MyPageResponse(
        UserInfo user,
        Stats stats,
        List<ComplaintSummaryResponse> recentComplaints
) {
    public record UserInfo(Long userId, String loginId, String name, String email) {
    }

    public record Stats(long reportedCount, long resolvedCount, long receivedEmpathyCount) {
    }
}
