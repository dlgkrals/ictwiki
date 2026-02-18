package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.PasswordChangeRequest;
import com.ict.wiki.login.dto.request.PasswordResetRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.service.AuthService;
import com.ict.wiki.login.service.EmailVerificationService;
import com.ict.wiki.login.service.PasswordResetService;
import com.ict.wiki.security.auth.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    // 세션에 저장할 사용자 정보 키
    public static final String SESSION_USER_KEY = "loginUser";

    /**
     * CSRF 토큰 발급
     * GET /api/auth/csrf-token
     *
     * 프론트엔드 앱 시작 시 가장 먼저 호출
     * CSRF 토큰을 쿠키와 응답에 포함하여 반환
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        log.info("========== CSRF 토큰 발급 시작 ==========");
        log.info("세션 ID: {}", session.getId());
        log.info("세션 생성 시간: {}", new java.util.Date(session.getCreationTime()));
        log.info("세션 isNew: {}", session.isNew());

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            csrfToken = (CsrfToken) request.getAttribute("_csrf");
        }

        String token = csrfToken != null ? csrfToken.getToken() : "";

        log.info("발급된 CSRF 토큰: {}", token);
        log.info("CSRF 토큰 헤더 이름: {}", csrfToken != null ? csrfToken.getHeaderName() : "null");
        log.info("========== CSRF 토큰 발급 완료 ==========");

        return ResponseEntity.ok(Map.of("csrfToken", token, "sessionId", session.getId()));
    }

    /**
     * 회원가입
     * ⭐ 성공 시 인증 메일 발송
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        return ResponseEntity.ok(Map.of(
                "message", "회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.",
                "email", request.getEmail()
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
     * POST /api/auth/resend-verification?email=student@gmail.com
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);

        return ResponseEntity.ok(Map.of(
                "message", "인증 메일이 재발송되었습니다.",
                "email", email
        ));
    }

    // ⭐⭐⭐ /login 엔드포인트 완전 제거! ⭐⭐⭐
    // → JsonAuthenticationFilter가 처리함

    /**
     * 로그아웃
     * ⭐ Security가 처리하도록 변경 예정 (현재는 유지)
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
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(UserResponse.from(userDetails.getUser()));
    }

    /**
     * 이메일 중복 체크
     * GET /api/auth/check-email?email=student@gmail.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean isDuplicate = authService.checkEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    // ⭐ CSRF 토큰 엔드포인트 제거!
    // → Security가 자동으로 처리

    /**
     * 비밀번호 재설정 이메일 발송
     * POST /api/auth/forgot-password?email=student@gmail.com
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        passwordResetService.sendPasswordResetEmail(email);

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호 재설정 메일이 발송되었습니다.",
                "email", email
        ));
    }

    /**
     * 비밀번호 재설정 토큰 검증
     * GET /api/auth/reset-password?token=xxx
     */
    @GetMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);

        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "유효하지 않거나 만료된 토큰입니다"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "유효한 토큰입니다"
        ));
    }

    /**
     * 비밀번호 재설정 완료
     * POST /api/auth/reset-password
     * Body: { "token": "xxx", "newPassword": "NewPass123!@#" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요."
        ));
    }

    /**
     * 비밀번호 변경 (로그인 상태)
     * PUT /api/auth/change-password
     * Body: { "currentPassword": "Old123!@#", "newPassword": "New123!@#" }
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 비밀번호 변경
        authService.changePassword(
                userDetails.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 성공적으로 변경되었습니다."
        ));
    }
}