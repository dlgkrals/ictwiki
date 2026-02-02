package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.response.UserSummaryResponse;
import com.ict.wiki.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * STAFF 목록 조회
     * GET /api/users/staff
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
