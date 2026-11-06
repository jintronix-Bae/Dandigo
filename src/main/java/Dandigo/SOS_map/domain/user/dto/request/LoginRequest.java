package Dandigo.SOS_map.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

// 형식 규칙은 넣지 않는다(가입 규칙 노출 방지). 비어 있는지만 검사한다.
public record LoginRequest(
        @NotBlank(message = "아이디를 입력해 주세요.") String loginId,
        @NotBlank(message = "비밀번호를 입력해 주세요.") String password
) {
}
