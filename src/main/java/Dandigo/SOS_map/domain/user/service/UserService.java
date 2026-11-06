package Dandigo.SOS_map.domain.user.service;

import Dandigo.SOS_map.domain.user.dto.request.SignUpRequest;
import Dandigo.SOS_map.domain.user.dto.response.EmailCheckResponse;
import Dandigo.SOS_map.domain.user.dto.response.LoginIdCheckResponse;
import Dandigo.SOS_map.domain.user.dto.response.SignUpResponse;
import Dandigo.SOS_map.domain.user.entity.Member;
import Dandigo.SOS_map.domain.user.repository.MemberRepository;
import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// 회원가입, 중복 확인, 마이페이지 기능관련 서비스 코드
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // #1.회원가입 (1번)
    @Transactional
    public SignUpResponse register(SignUpRequest request) {
        String email = normalizeEmail(request.email());

        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        // 동시에 같은 값으로 가입하면 위 검사를 둘 다 통과할 수 있다.
        // 그 경우는 DB UK에 걸리고 GlobalExceptionHandler #5가 409로 응답한다.
        Member member = Member.register(
                request.loginId(),
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()));
        return SignUpResponse.from(memberRepository.save(member));
    }

    // #2.이메일 중복 확인 (2번)
    public EmailCheckResponse checkEmail(String email) {
        String normalized = normalizeEmail(email);
        return new EmailCheckResponse(normalized, !memberRepository.existsByEmail(normalized));
    }

    // #3.아이디 중복 확인 (3번)
    public LoginIdCheckResponse checkLoginId(String loginId) {
        return new LoginIdCheckResponse(loginId, !memberRepository.existsByLoginId(loginId));
    }

    // 이메일은 앞뒤 공백을 지우고 소문자로 맞춰서 비교·저장한다.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}