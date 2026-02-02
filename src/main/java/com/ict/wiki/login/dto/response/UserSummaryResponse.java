package com.ict.wiki.login.dto.response;

import com.ict.wiki.login.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String name;
    private String email;

    public static UserSummaryResponse from(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
