package com.ict.wiki.admin.controller;

import com.ict.wiki.admin.dto.response.UserManagementResponse;
import com.ict.wiki.admin.service.AdminUserService;
import com.ict.wiki.login.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 전체 사용자 목록 조회 (관리자용)
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<List<UserManagementResponse>> getAllUsers() {
        List<UserManagementResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 승인
     * PUT /api/admin/users/{userId}/approve
     */
    @PutMapping("/{userId}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable Long userId) {
        adminUserService.approveUser(userId);
        return ResponseEntity.ok(Map.of("message", "사용자가 승인되었습니다."));
    }

    /**
     * 사용자 비활성화
     * PUT /api/admin/users/{userId}/deactivate
     */
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long userId) {
        adminUserService.deactivateUser(userId);
        return ResponseEntity.ok(Map.of("message", "사용자가 비활성화되었습니다."));
    }

    /**
     * 사용자 활성화
     * PUT /api/admin/users/{userId}/activate
     */
    @PutMapping("/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long userId) {
        adminUserService.activateUser(userId);
        return ResponseEntity.ok(Map.of("message", "사용자가 활성화되었습니다."));
    }

    /**
     * 사용자 삭제
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
    }

    /**
     * 사용자 비밀번호 강제 변경
     * PUT /api/admin/users/{userId}/password
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        adminUserService.changePassword(userId, request.get("password"));
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
    }

    /**
     * 사용자 역할 변경
     * PUT /api/admin/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<Map<String, String>> changeRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        Role newRole = Role.valueOf(request.get("role"));
        adminUserService.changeRole(userId, newRole);
        return ResponseEntity.ok(Map.of("message", "역할이 변경되었습니다."));
    }
}