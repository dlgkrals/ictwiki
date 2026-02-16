package com.ict.wiki.admin.dto.response;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 회원 정보 응답 DTO
 * - 일반 UserResponse보다 더 많은 정보 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class UserManagementResponse {

    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Role role;

    // 계정 상태
    private boolean active;
    private boolean approved;  // ⭐ 승인 여부

    // 날짜 정보
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * User 엔티티에서 DTO 생성
     */
    public static UserManagementResponse from(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .active(user.isActive())
                .approved(user.isApproved())
                .createdAt(user.getCreatedAt())
                .modifiedAt(user.getModifiedAt())
                .build();
    }
}