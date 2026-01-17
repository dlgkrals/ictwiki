package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.service.AuthService;
import com.ict.wiki.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionUtil sessionUtil;

    // 세션에 저장할 사용자 정보 키
    public static final String SESSION_USER_KEY = "loginUser";

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다");
    }

    /**
     * 로그인
     * 성공 시 세션에 사용자 정보 저장
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        // 인증 처리
        User user = authService.login(request);

        // 세션에 사용자 정보 저장
        session.setAttribute(SESSION_USER_KEY, user.getId());
        session.setMaxInactiveInterval(30 * 60);

        return ResponseEntity.ok(LoginResponse.from(user));
    }

    /**
     * 로그아웃
     * 세션 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃되었습니다");
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpSession session) {
        User user = sessionUtil.getCurrentUser(session);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String emailPrefix) {
        boolean isDuplicate = authService.checkEmailDuplicate(emailPrefix);
        return ResponseEntity.ok(isDuplicate);
    }
}