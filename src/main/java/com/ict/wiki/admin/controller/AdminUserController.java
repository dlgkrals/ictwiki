package com.ict.wiki.admin.controller;

import com.ict.wiki.admin.dto.response.UserManagementResponse;
import com.ict.wiki.admin.service.AdminUserService;
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
@PreAuthorize("hasRole('ADMIN')")  // ⭐ 클래스 레벨: 모든 API는 관리자만 접근
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
}