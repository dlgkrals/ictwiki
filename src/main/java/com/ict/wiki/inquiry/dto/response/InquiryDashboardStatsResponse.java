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

    /** 연도 모드: 연간 일평균 / 당월 모드: 월 일평균 / 당주 모드: 주 일평균 */
    private Long avgDailyCount;

    /** 연도 모드: 연간 주평균 / 당월 모드: 월 주평균 / 당주 모드: null */
    private Long avgWeeklyCount;

    /** 연도 모드: 연간 월평균 / 당월·당주 모드: null */
    private Long avgMonthlyCount;

    /** 당월·당주 모드: 이번 달 건수 / 연도 모드: null */
    private Long currentMonthCount;

    /** 당주 모드: 해당 주차 건수 / 나머지: null */
    private Long currentWeekCount;
}