package com.ict.wiki.inquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class InquiryDashboardStatsResponse {

    // ===== 전체 통계 =====

    /** 전체 민원 수 */
    private Long totalCount;

    /** 상태별 건수 */
    private Map<String, Long> statusCounts;

    /** 유형별 건수 */
    private List<InquiryStatsResponse> typeCounts;

    /** 처리방식별 건수 */
    private List<InquiryStatsResponse> methodCounts;

    /** 건물별 건수 */
    private List<InquiryStatsResponse> buildingCounts;

    // ===== 평균 통계 =====

    /** 일평균 민원 수 */
    private Long avgDailyCount;

    /** 주평균 민원 수 */
    private Long avgWeeklyCount;

    /** 월평균 민원 수 */
    private Long avgMonthlyCount;
}