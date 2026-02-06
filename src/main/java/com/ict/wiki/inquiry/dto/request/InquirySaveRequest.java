package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.InquiryMethod;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.domain.InquiryType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @NotBlank(message = "요청자를 입력하세요")
    private String requester;

    // 건물 및 호실 (선택 항목)
    private String buildingCode;      // ← 추가

    @Min(value = 1, message = "호실은 숫자여야 합니다")  // ← 추가
    private Integer roomNumber;       // ← 추가

    // ===== 선택 항목 (생성 시 nullable, 수정 시 사용) =====
    private InquiryStatus status;
    private Long workerId;
    private LocalDate workDate;
    private InquiryMethod method;
    private String solution;
}