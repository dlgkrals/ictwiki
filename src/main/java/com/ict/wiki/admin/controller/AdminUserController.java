package com.ict.wiki.admin.controller;

import com.ict.wiki.admin.dto.request.AdminPasswordChangeRequest;
import com.ict.wiki.admin.dto.response.AdminUserResponse;
import com.ict.wiki.admin.service.AdminUserService;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 회원 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // ⭐ 변경: 모든 API는 관리자만 접근
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ========== 회원 목록 조회 ==========

    /**
     * 전체 회원 목록
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<User> users = adminUserService.getAllUsers();
        List<AdminUserResponse> response = users.stream()
                .map(AdminUserResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 승인 대기 회원 목록
     * GET /api/admin/users/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AdminUserResponse>> getPendingUsers() {
        List<User> users = adminUserService.getPendingUsers();
        List<AdminUserResponse> response = users.stream()
                .map(AdminUserResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 회원 상세 조회
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUser(@PathVariable Long userId) {
        User user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(AdminUserResponse.from(user));
    }

    // ========== 회원 승인 ==========

    /**
     * 회원 승인
     * POST /api/admin/users/{userId}/approve
     */
    @PostMapping("/{userId}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable Long userId) {
        adminUserService.approveUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "회원이 승인되었습니다",
                "userId", userId.toString()
        ));
    }

    /**
     * 회원 승인 거부
     * POST /api/admin/users/{userId}/reject
     */
    @PostMapping("/{userId}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(@PathVariable Long userId) {
        adminUserService.rejectUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "회원 승인이 거부되었습니다",
                "userId", userId.toString()
        ));
    }

    // ========== 계정 활성화/비활성화 ==========

    /**
     * 계정 활성화
     * POST /api/admin/users/{userId}/activate
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long userId) {
        adminUserService.activateUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "계정이 활성화되었습니다",
                "userId", userId.toString()
        ));
    }

    /**
     * 계정 비활성화
     * POST /api/admin/users/{userId}/deactivate
     */
    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long userId) {
        adminUserService.deactivateUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "계정이 비활성화되었습니다",
                "userId", userId.toString()
        ));
    }

    // ========== 비밀번호 변경 ==========

    /**
     * 관리자가 회원 비밀번호 강제 변경
     * PUT /api/admin/users/{userId}/password
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, String>> changeUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody AdminPasswordChangeRequest request) {

        adminUserService.changePassword(userId, request.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 변경되었습니다",
                "userId", userId.toString()
        ));
    }

    // ========== 권한 변경 ==========

    /**
     * 회원 권한 변경
     * PUT /api/admin/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable Long userId,
            @RequestParam Role role) {

        adminUserService.changeRole(userId, role);

        return ResponseEntity.ok(Map.of(
                "message", "권한이 변경되었습니다",
                "userId", userId.toString(),
                "role", role.name()
        ));
    }

    // ========== 회원 삭제 ==========

    /**
     * 회원 삭제 (소프트 삭제 - active = false)
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "회원이 삭제되었습니다",
                "userId", userId.toString()
        ));
    }

    // ========== 통계 ==========

    /**
     * 회원 통계
     * GET /api/admin/users/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = adminUserService.getUserStats();
        return ResponseEntity.ok(stats);
    }
}