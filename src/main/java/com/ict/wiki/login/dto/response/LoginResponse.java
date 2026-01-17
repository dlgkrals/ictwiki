package com.ict.wiki.login.dto.response;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String name;
    private String email;
    private Role role;

    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
