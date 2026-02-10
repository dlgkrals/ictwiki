package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.UserUpdateRequest;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.dto.response.UserSummaryResponse;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.login.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;  // ⭐ 추가

    /**
     * 현재 로그인한 사용자 조회 (헬퍼 메서드)
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 내 정보 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User currentUser = getCurrentUser(userDetails);
        return ResponseEntity.ok(UserResponse.from(currentUser));
    }

    /**
     * 내 정보 수정 (이름, 전화번호만)
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User currentUser = getCurrentUser(userDetails);
        User updatedUser = userService.updateUserInfo(currentUser.getId(), request);

        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    /**
     * STAFF 목록 조회
     */
    @GetMapping("/staff")
    public ResponseEntity<List<UserSummaryResponse>> getStaffUsers() {
        List<User> staffUsers = userService.getStaffUsers();

        List<UserSummaryResponse> response = staffUsers.stream()
                .map(UserSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}