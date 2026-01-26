package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.service.AuthService;
import com.ict.wiki.login.service.EmailVerificationService;
import com.ict.wiki.login.service.SessionManagementService;
import com.ict.wiki.util.CsrfTokenUtil;
import com.ict.wiki.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionUtil sessionUtil;
    private final SessionManagementService sessionManagementService;
    private final EmailVerificationService emailVerificationService;  // ⭐ 추가

    // 세션에 저장할 사용자 정보 키
    public static final String SESSION_USER_KEY = "loginUser";

    /**
     * 회원가입
     * ⭐ 성공 시 인증 메일 발송
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        // ⭐ 응답 메시지 변경
        return ResponseEntity.ok(Map.of(
                "message", "회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.",
                "email", request.getFullEmail()
        ));
    }

    /**
     * ⭐ 이메일 인증 처리
     * GET /api/auth/verify-email?token=xxx
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(Map.of(
                "message", "이메일 인증이 완료되었습니다. 로그인해주세요."
        ));
    }

    /**
     * ⭐ 인증 메일 재발송
     * POST /api/auth/resend-verification?emailPrefix=student
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestParam String emailPrefix) {
        String fullEmail = emailPrefix + "@g.seoil.ac.kr";
        emailVerificationService.resendVerificationEmail(fullEmail);

        return ResponseEntity.ok(Map.of(
                "message", "인증 메일이 재발송되었습니다.",
                "email", fullEmail
        ));
    }

    /**
     * 로그인
     * 성공 시 세션 ID 재생성 후 사용자 정보 저장 + 새로운 CSRF 토큰 발급
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 인증 처리
        User user = authService.login(request);

        // ⭐ 세션 고정 공격 방지: 기존 세션 무효화 후 새 세션 생성
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();  // 기존 세션 무효화
        }

        // 새로운 세션 생성
        HttpSession newSession = httpRequest.getSession(true);
        String newSessionId = newSession.getId();

        // ⭐ 중복 로그인 방지: 기존 세션 무효화 및 새 세션 등록
        sessionManagementService.registerSession(user.getEmail(), newSessionId);

        // 새 세션에 사용자 정보 저장
        newSession.setAttribute(SESSION_USER_KEY, user.getId());
        newSession.setMaxInactiveInterval(30 * 60); // 30분

        // 로그인 후 새로운 CSRF 토큰 발급
        String csrfToken = CsrfTokenUtil.generateToken(newSession);

        Map<String, Object> response = new HashMap<>();
        response.put("user", LoginResponse.from(user));
        response.put("csrfToken", csrfToken);

        log.info("로그인 성공 - Email: {}, SessionId: {}", user.getEmail(), newSessionId);

        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     * 세션 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        String sessionId = session.getId();
        sessionManagementService.removeSession(sessionId);

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