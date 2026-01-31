package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.InquiryMethod;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.domain.InquiryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 민원 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryUpdateRequest {

    @NotBlank(message = "작업 이름을 입력하세요")
    private String title;

    @NotNull(message = "유형을 선택하세요")
    private InquiryType type;

    @NotNull(message = "상태를 선택하세요")
    private InquiryStatus status;

    private Long workerId;  // nullable (미배정 가능)

    private LocalDate workDate;

    private InquiryMethod method;

    @NotBlank(message = "문제 설명을 입력하세요")
    private String description;

    private String solution;

    @NotBlank(message = "요청자를 입력하세요")
    private String requester;
}