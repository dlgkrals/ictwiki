package com.ict.wiki.login.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
public class LoginRequest {

    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;  // 전체 이메일 (예: student@gmail.com)

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;
}
