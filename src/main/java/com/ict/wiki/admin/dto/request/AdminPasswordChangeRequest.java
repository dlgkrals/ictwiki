package com.ict.wiki.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자가 회원 비밀번호 강제 변경 요청 DTO
 */
@Getter
@Setter
public class AdminPasswordChangeRequest {

    @NotBlank(message = "새 비밀번호를 입력하세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자여야 합니다")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다"
    )
    private String newPassword;
}