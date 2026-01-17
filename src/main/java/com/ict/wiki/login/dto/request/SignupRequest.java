package com.ict.wiki.login.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일 아이디를 입력하세요")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "이메일은 영문, 숫자, 특수문자(._-)만 사용 가능합니다")
    @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다")
    private String emailPrefix; // "student" 부분만

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    @NotBlank(message = "이름을 입력하세요")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{2,30}$",
            message = "이름은 한글, 영문만 사용 가능하며 2-30자여야 합니다")
    private String name;

    @Pattern(regexp = "^[0-9]{8,12}$",
            message = "학번은 8-12자리 숫자만 가능합니다")
    private String studentId;

    @Pattern(regexp = "^[가-힣a-zA-Z\\s()]{2,50}$",
            message = "학과명은 한글, 영문만 사용 가능하며 2-50자여야 합니다")
    private String department;

    // 전체 이메일 생성
    public String getFullEmail() {
        return emailPrefix + "@g.seoil.ac.kr";
    }
}