package Dandigo.SOS_map.domain.user.dto.response;

import Dandigo.SOS_map.domain.user.entity.Member;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,      // 초 단위
        UserSummary user
) {
    private static final String BEARER_TYPE = "Bearer";

    public record UserSummary(Long userId, String loginId, String name) {}

    public static LoginResponse of(String accessToken, long expirationMillis, Member member) {
        return new LoginResponse(
                accessToken,
                BEARER_TYPE,
                expirationMillis / 1000,
                new UserSummary(member.getId(), member.getLoginId(), member.getName()));
    }
}