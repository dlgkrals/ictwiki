package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.InquiryMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 작업자 배정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignWorkerRequest {

    @NotNull(message = "작업자를 선택하세요")
    private Long workerId;

    @NotNull(message = "처리 방식을 선택하세요")
    private InquiryMethod method;
}