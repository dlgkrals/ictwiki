package com.ict.wiki.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 민원 완료 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteInquiryRequest {

    @NotBlank(message = "해결 방법을 입력하세요")
    private String solution;
}