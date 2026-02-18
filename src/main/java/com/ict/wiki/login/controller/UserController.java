package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.UserUpdateRequest;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.dto.response.UserSummaryResponse;
import com.ict.wiki.login.service.UserService;
import com.ict.wiki.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        // ⭐ SecurityContext에서 직접 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "email", userDetails.getUsername(),
                "name", userDetails.getName(),
                "role", userDetails.getRole().name()
        ));
    }

    /**
     * 내 정보 수정 (이름, 전화번호만)
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User updatedUser = userService.updateUserInfo(userDetails.getId(), request);

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
