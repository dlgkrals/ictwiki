package com.ict.wiki.inquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 민원 통계 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InquiryStatsResponse {

    /**
     * 통계 항목 이름 (예: "PC", "원격", "2025-02", "흥학관")
     */
    private String label;

    /**
     * 건수
     */
    private Long count;

    /**
     * 비율 (퍼센트, 소수점 2자리)
     */
    private Double percentage;
}