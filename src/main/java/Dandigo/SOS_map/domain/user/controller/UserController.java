package Dandigo.SOS_map.domain.user.controller;

import Dandigo.SOS_map.domain.user.dto.request.LoginRequest;
import Dandigo.SOS_map.domain.user.dto.request.SignUpRequest;
import Dandigo.SOS_map.domain.user.dto.response.EmailCheckResponse;
import Dandigo.SOS_map.domain.user.dto.response.LoginIdCheckResponse;
import Dandigo.SOS_map.domain.user.dto.response.LoginResponse;
import Dandigo.SOS_map.domain.user.dto.response.MyPageResponse;
import Dandigo.SOS_map.domain.user.dto.response.SignUpResponse;
import Dandigo.SOS_map.domain.user.service.AuthService;
import Dandigo.SOS_map.domain.user.service.UserService;
import Dandigo.SOS_map.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 회원가입, 중복 확인, 로그인, 마이페이지 API (1~5번)
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // #1.회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    // #2.이메일 중복 확인 (중복이어도 200, available=false)
    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.checkEmail(email));
    }

    // #3.아이디 중복 확인 (중복이어도 200, available=false)
    @GetMapping("/check-login-id")
    public ResponseEntity<LoginIdCheckResponse> checkLoginId(@RequestParam String loginId) {
        return ResponseEntity.ok(userService.checkLoginId(loginId));
    }

    // #4.로그인 (실패 시 401 LOGIN_FAILED)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

}