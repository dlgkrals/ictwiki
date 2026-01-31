package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.InquiryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 민원 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCreateRequest {

    @NotBlank(message = "작업 이름을 입력하세요")
    private String title;

    @NotNull(message = "유형을 선택하세요")
    private InquiryType type;

    @NotBlank(message = "문제 설명을 입력하세요")
    private String description;

    @NotBlank(message = "요청자를 입력하세요")
    private String requester;
}