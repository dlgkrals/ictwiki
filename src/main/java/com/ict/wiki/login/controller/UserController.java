package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.UserUpdateRequest;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.dto.response.UserSummaryResponse;
import com.ict.wiki.login.service.UserService;
import com.ict.wiki.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SessionUtil sessionUtil;

    /**
     * 내 정보 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(HttpSession session) {
        User currentUser = sessionUtil.getCurrentUser(session);
        return ResponseEntity.ok(UserResponse.from(currentUser));
    }

    /**
     * 내 정보 수정 (이름, 전화번호만)
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(
            @Valid @RequestBody UserUpdateRequest request,
            HttpSession session) {

        User currentUser = sessionUtil.getCurrentUser(session);
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
