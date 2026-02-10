package com.ict.wiki.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;      // Access Token (메모리 저장용)
    private String tokenType;        // Bearer
    private Long expiresIn;          // 만료 시간 (초 단위)

    // 사용자 정보
    private Long userId;
    private String email;
    private String name;
    private String role;
}