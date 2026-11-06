package Dandigo.SOS_map.domain.user.dto.response;

import Dandigo.SOS_map.domain.user.entity.Member;

public record SignUpResponse(Long userId, String loginId, String name, String email) {

    public static SignUpResponse from(Member member) {
        return new SignUpResponse(member.getId(), member.getLoginId(), member.getName(), member.getEmail());
    }
}
