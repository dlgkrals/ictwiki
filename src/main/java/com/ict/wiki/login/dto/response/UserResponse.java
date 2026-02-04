package com.ict.wiki.login.dto.response;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;

    // User 엔티티에서 DTO 생성
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
}
