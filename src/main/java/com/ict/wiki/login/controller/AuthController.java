package com.ict.wiki.login.controller;

import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.service.AuthService;
import com.ict.wiki.login.service.EmailVerificationService;
import com.ict.wiki.login.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    /**
     * 회원가입
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
     * 이메일 인증 처리
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(Map.of(
                "message", "이메일 인증이 완료되었습니다. 로그인해주세요."
        ));
    }

    /**
     * 인증 메일 재발송
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);

        return ResponseEntity.ok(Map.of(
                "message", "인증 메일이 재발송되었습니다."
        ));
    }

    /**
     * 로그인 (JWT)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(request, response);

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.refresh(request, response);

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {

        authService.logout(userDetails.getUsername(), response);

        return ResponseEntity.ok(Map.of(
                "message", "로그아웃되었습니다."
        ));
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(Map.of(
                "email", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities()
        ));
    }

    /**
     * 비밀번호 재설정 요청
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestParam String email) {
        passwordResetService.sendPasswordResetEmail(email);

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호 재설정 이메일이 발송되었습니다."
        ));
    }

    /**
     * 비밀번호 재설정 토큰 검증
     */
    @GetMapping("/password-reset/verify")
    public ResponseEntity<Map<String, String>> verifyResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);

        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "message", "유효한 토큰입니다."
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "유효하지 않거나 만료된 토큰입니다."
            ));
        }
    }

    /**
     * 새 비밀번호 설정
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody Map<String, String> request) {

        String token = request.get("token");
        String newPassword = request.get("newPassword");

        passwordResetService.resetPassword(token, newPassword);

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 재설정되었습니다."
        ));
    }
}