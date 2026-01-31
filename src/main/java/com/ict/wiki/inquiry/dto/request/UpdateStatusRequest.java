package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상태 변경 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "상태를 선택하세요")
    private InquiryStatus status;
}