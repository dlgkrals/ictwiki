package com.ict.wiki.login.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    @NotBlank(message = "이름을 입력하세요")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{2,30}$",
            message = "이름은 한글, 영문만 사용 가능하며 2-30자여야 합니다")
    private String name;

    @Pattern(regexp = "^01[0-9][0-9]{7,8}$",
            message = "올바른 핸드폰 번호 형식이 아닙니다 (예: 01012345678)")
    private String phoneNumber;
}