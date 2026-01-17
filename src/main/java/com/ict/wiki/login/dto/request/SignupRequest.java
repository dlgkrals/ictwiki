package com.ict.wiki.login.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일 아이디를 입력하세요")
    private String emailPrefix; // "student" 부분만

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    private String studentId;

    private String department;

    // 전체 이메일 생성
    public String getFullEmail() {
        return emailPrefix + "@g.seoil.ac.kr";
    }
}
