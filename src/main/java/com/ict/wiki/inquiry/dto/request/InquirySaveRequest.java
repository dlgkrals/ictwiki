package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.Building;
import com.ict.wiki.inquiry.domain.InquiryMethod;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.domain.InquiryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 민원 요청 dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquirySaveRequest {

    @NotBlank(message = "작업 이름을 입력하세요")
    private String title;

    @NotNull(message = "유형을 선택하세요")
    private InquiryType type;

    @NotBlank(message = "문제 설명을 입력하세요")
    private String description;

    private String requester;

    // ===== 선택 항목 (생성 시 nullable, 수정 시 사용) =====
    @Valid
    private List<LocationRequest> locations;

    private InquiryStatus status;
    private Long workerId;
    private Long subWorkerId;  // 보조 작업자 ID (선택)
    private InquiryMethod method;
    private String solution;
}