package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.login.service.LoginAttemptService;
import com.ict.wiki.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 전용 API
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    /**
     * 전체 사용자 목록 조회 (관리자만 가능)
     */
    @RequireRole(Role.ADMIN)
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 계정 비활성화 (관리자만 가능)
     */
    @RequireRole(Role.ADMIN)
    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.deactivate();
        userRepository.save(user);

        return ResponseEntity.ok("사용자가 비활성화되었습니다");
    }

    /**
     * 통계 조회 (교직원, 관리자 가능)
     */
    @RequireRole({Role.STAFF, Role.ADMIN})
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        long totalUsers = userRepository.count();
        long trackedAccounts = loginAttemptService.getTotalTrackedAccounts();
        long lockedAccounts = loginAttemptService.getLockedAccountsCount();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "trackedAccounts", trackedAccounts,
                "lockedAccounts", lockedAccounts,
                "message", "통계 정보"
        ));
    }

    // ============ 개선된 API (userId 기반) ============

    /**
     * 특정 사용자의 로그인 시도 정보 조회 (관리자만 가능)
     * RESTful 방식: userId 사용
     */
    @RequireRole(Role.ADMIN)
    @GetMapping("/users/{userId}/login-attempts")
    public ResponseEntity<Map<String, Object>> getLoginAttemptsByUserId(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        String email = user.getEmail();
        boolean isLocked = loginAttemptService.isLocked(email);
        int failCount = loginAttemptService.getFailCount(email);
        long remainingTime = loginAttemptService.getRemainingLockTime(email);
        int remainingAttempts = loginAttemptService.getRemainingAttempts(email);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email,
                "name", user.getName(),
                "isLocked", isLocked,
                "failCount", failCount,
                "remainingLockSeconds", remainingTime,
                "remainingAttempts", remainingAttempts
        ));
    }

    /**
     * 계정 잠금 강제 해제 (관리자만 가능)
     * RESTful 방식: userId 사용
     */
    @RequireRole(Role.ADMIN)
    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<String> unlockUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        loginAttemptService.unlockUser(user.getEmail());

        return ResponseEntity.ok("계정 잠금이 해제되었습니다");
    }

    // ============ 기존 API (하위 호환성 유지) ============

    /**
     * 특정 사용자의 로그인 시도 정보 조회 (관리자만 가능)
     * 기존 방식: 쿼리 파라미터 (하위 호환성)
     */
    @RequireRole(Role.ADMIN)
    @GetMapping("/users/login-attempts")
    public ResponseEntity<Map<String, Object>> getLoginAttempts(@RequestParam String email) {
        boolean isLocked = loginAttemptService.isLocked(email);
        int failCount = loginAttemptService.getFailCount(email);
        long remainingTime = loginAttemptService.getRemainingLockTime(email);
        int remainingAttempts = loginAttemptService.getRemainingAttempts(email);

        return ResponseEntity.ok(Map.of(
                "email", email,
                "isLocked", isLocked,
                "failCount", failCount,
                "remainingLockSeconds", remainingTime,
                "remainingAttempts", remainingAttempts
        ));
    }

    /**
     * 계정 잠금 강제 해제 (관리자만 가능)
     * 기존 방식: 쿼리 파라미터 (하위 호환성)
     */
    @RequireRole(Role.ADMIN)
    @PostMapping("/users/unlock")
    public ResponseEntity<String> unlockUser(@RequestParam String email) {
        // 사용자 존재 확인
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 잠금 해제
        loginAttemptService.unlockUser(email);

        return ResponseEntity.ok("계정 잠금이 해제되었습니다");
    }
}