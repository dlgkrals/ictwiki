package com.ict.wiki.login.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 개인정보 수정 요청 DTO
 * - 이름, 전화번호만 수정 가능
 */
@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "이름을 입력하세요")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{2,30}$",
            message = "이름은 한글, 영문만 사용 가능하며 2-30자여야 합니다")
    private String name;

    @Pattern(regexp = "^01[0-9][0-9]{7,8}$",
            message = "올바른 핸드폰 번호 형식이 아닙니다 (예: 01012345678)")
    private String phoneNumber;
}