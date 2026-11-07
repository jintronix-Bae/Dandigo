package Dandigo.SOS_map.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotNull(message = "아이디는 영문, 숫자, 점, 밑줄, 하이픈을 사용해 4~20자로 입력해 주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{4,20}$",  // 위의 아이디 관련 검증하는 코드
                message = "아이디는 영문, 숫자, 점, 밑줄, 하이픈을 사용해 4~20자로 입력해 주세요.")
        String loginId,

        @NotBlank(message = "이름은 두 글자 이상 입력해 주세요.")
        @Size(min = 2, max = 20, message = "이름은 두 글자 이상 입력해 주세요.")
        String name,

        @NotBlank(message = "올바른 이메일 주소를 입력해 주세요.")
        @Email(message = "올바른 이메일 주소를 입력해 주세요.")
        @Size(max = 100, message = "올바른 이메일 주소를 입력해 주세요.")
        String email,

        @NotNull(message = "비밀번호는 영문과 숫자를 포함해 8자 이상 입력해 주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$", // 영문 및 숫자 포함하는지 검증하는 코드
                message = "비밀번호는 영문과 숫자를 포함해 8자 이상 입력해 주세요.")
        String password
) {
    // 이름은 앞뒤 공백을 뺀 값으로 "2자 이상"을 검사·저장한다. ("  a  " 가 통과하지 않도록)
    public SignUpRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}