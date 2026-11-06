package Dandigo.SOS_map.domain.user.service;

import Dandigo.SOS_map.domain.user.dto.request.LoginRequest;
import Dandigo.SOS_map.domain.user.dto.response.LoginResponse;
import Dandigo.SOS_map.domain.user.entity.Member;
import Dandigo.SOS_map.domain.user.repository.MemberRepository;
import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import Dandigo.SOS_map.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 로그인(4번): 아이디·비밀번호 확인 후 JWT 발급
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    // #1.로그인
    public LoginResponse login(LoginRequest request) {

        // 1) 아이디·비밀번호 검증 (틀리면 AuthenticationException → 401 LOGIN_FAILED)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));

        // 2) 토큰과 응답에 넣을 회원 정보 조회
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        // 3) Access Token 발급
        String accessToken = jwtProvider.generateAccessToken(
                member.getId(), member.getLoginId(), member.getRole());

        return LoginResponse.of(accessToken, jwtProvider.getAccessTokenExpiration(), member);
    }
}