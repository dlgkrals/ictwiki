package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.LoginRequest;
import com.ict.wiki.login.dto.SignupRequest;
import com.ict.wiki.login.service.AuthService;
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
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        // 인증 처리
        User user = authService.login(request);

        // 세션에 사용자 정보 저장
        session.setAttribute(SESSION_USER_KEY, user.getId());

        // 세션 타임아웃 설정 (30분)
        session.setMaxInactiveInterval(30 * 60);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("message", "로그인 성공");

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
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다"));
        }

        // 실제로는 UserService를 통해 사용자 정보를 조회해야 하지만
        // 간단히 세션 정보만 반환
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("message", "인증된 사용자");

        return ResponseEntity.ok(response);
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