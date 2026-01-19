package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.service.AuthService;
import com.ict.wiki.util.CsrfTokenUtil;
import com.ict.wiki.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
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
     * 성공 시 세션 ID 재생성 후 사용자 정보 저장 + 새로운 CSRF 토큰 발급
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {  // ← HttpServletRequest 추가

        // 인증 처리
        User user = authService.login(request);

        // ⭐ 세션 고정 공격 방지: 기존 세션 무효화 후 새 세션 생성
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();  // 기존 세션 무효화
        }

        // 새로운 세션 생성
        HttpSession newSession = httpRequest.getSession(true);

        // 새 세션에 사용자 정보 저장
        newSession.setAttribute(SESSION_USER_KEY, user.getId());
        newSession.setMaxInactiveInterval(30 * 60); // 30분

        // 로그인 후 새로운 CSRF 토큰 발급
        String csrfToken = CsrfTokenUtil.generateToken(newSession);

        Map<String, Object> response = new HashMap<>();
        response.put("user", LoginResponse.from(user));
        response.put("csrfToken", csrfToken);

        return ResponseEntity.ok(response);
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

    /**
     * CSRF 토큰 발급
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpSession session) {
        String token = CsrfTokenUtil.generateToken(session);
        return ResponseEntity.ok(Map.of("csrfToken", token));
    }
}