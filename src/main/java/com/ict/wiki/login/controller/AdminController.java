package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.repository.UserRepository;
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

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "message", "통계 정보"
        ));
    }
}



