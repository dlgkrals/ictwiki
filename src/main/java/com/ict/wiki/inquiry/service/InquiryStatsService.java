package com.ict.wiki.inquiry.service;

import com.ict.wiki.inquiry.domain.Building;
import com.ict.wiki.inquiry.domain.InquiryMethod;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.domain.InquiryType;
import com.ict.wiki.inquiry.dto.response.InquiryDashboardStatsResponse;
import com.ict.wiki.inquiry.dto.response.InquiryStatsResponse;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 민원 통계 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStatsService {

    private final InquiryRepository inquiryRepository;

    // ========== 시간별 통계 ==========

    /**
     * 일별 민원 건수
     */
    public long getDailyCount(LocalDate date) {
        return inquiryRepository.countByDate(date);
    }

    /**
     * 월별 민원 건수
     */
    public long getMonthlyCount(int year, int month) {
        return inquiryRepository.countByYearAndMonth(year, month);
    }

    /**
     * 연별 민원 건수
     */
    public long getYearlyCount(int year) {
        return inquiryRepository.countByYear(year);
    }

    /**
     * 기간별 일일 통계
     */
    public List<InquiryStatsResponse> getDailyStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = inquiryRepository.countByDateRange(start, end);

        long totalCount = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return results.stream()
                .map(result -> InquiryStatsResponse.builder()
                        .label(result[0].toString())
                        .count(((Number) result[1]).longValue())
                        .percentage(calculatePercentage(((Number) result[1]).longValue(), totalCount))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 연도의 월별 통계
     */
    public List<InquiryStatsResponse> getMonthlyStatsInYear(int year) {
        List<Object[]> results = inquiryRepository.countByMonthsInYear(year);

        long totalCount = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return results.stream()
                .map(result -> {
                    int month = ((Number) result[0]).intValue();
                    long count = ((Number) result[1]).longValue();

                    return InquiryStatsResponse.builder()
                            .label(year + "-" + String.format("%02d", month))
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 유형별 통계 ==========

    /**
     * 유형별 통계
     */
    public List<InquiryStatsResponse> getTypeStats() {
        List<Object[]> results = inquiryRepository.countByType();

        long totalCount = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return results.stream()
                .map(result -> {
                    InquiryType type = (InquiryType) result[0];
                    long count = ((Number) result[1]).longValue();

                    return InquiryStatsResponse.builder()
                            .label(type.getDescription())
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 처리방식별 통계 ==========

    /**
     * 처리방식별 통계 (원격/방문)
     */
    public List<InquiryStatsResponse> getMethodStats() {
        List<Object[]> results = inquiryRepository.countByMethod();

        long totalCount = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return results.stream()
                .map(result -> {
                    InquiryMethod method = (InquiryMethod) result[0];
                    long count = ((Number) result[1]).longValue();

                    return InquiryStatsResponse.builder()
                            .label(method.getDescription())
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 건물별 통계 ==========

    /**
     * 건물별 통계
     */
    public List<InquiryStatsResponse> getBuildingStats() {
        List<Object[]> results = inquiryRepository.countByBuilding();

        long totalCount = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return results.stream()
                .map(result -> {
                    Building building = (Building) result[0];
                    long count = ((Number) result[1]).longValue();

                    return InquiryStatsResponse.builder()
                            .label(building.getDisplayName())
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 상태별 통계 ==========

    /**
     * 상태별 통계
     */
    public List<InquiryStatsResponse> getStatusStats() {
        List<Object[]> results = inquiryRepository.countByStatus();

        long totalCount = results.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return results.stream()
                .map(result -> {
                    InquiryStatus status = (InquiryStatus) result[0];
                    long count = ((Number) result[1]).longValue();

                    return InquiryStatsResponse.builder()
                            .label(status.getDescription())
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 종합 대시보드 통계 ==========

    /**
     * 대시보드용 종합 통계
     */
    public InquiryDashboardStatsResponse getDashboardStats() {
        // 전체 건수
        long totalCount = inquiryRepository.countAll();

        // 상태별 건수 (Map 형태)
        Map<String, Long> statusCounts = inquiryRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        result -> ((InquiryStatus) result[0]).getDescription(),
                        result -> ((Number) result[1]).longValue()
                ));

        // 유형별 통계
        List<InquiryStatsResponse> typeCounts = getTypeStats();

        // 처리방식별 통계
        List<InquiryStatsResponse> methodCounts = getMethodStats();

        // 건물별 통계
        List<InquiryStatsResponse> buildingCounts = getBuildingStats();

        // 평균 통계 계산
        Double avgDailyCount = calculateAvgDailyCount(totalCount);
        Double avgMonthlyCount = calculateAvgMonthlyCount(totalCount);
        Double avgProcessingDays = inquiryRepository.getAvgProcessingDays();

        return InquiryDashboardStatsResponse.builder()
                .totalCount(totalCount)
                .statusCounts(statusCounts)
                .typeCounts(typeCounts)
                .methodCounts(methodCounts)
                .buildingCounts(buildingCounts)
                .avgDailyCount(avgDailyCount)
                .avgMonthlyCount(avgMonthlyCount)
                .avgProcessingDays(avgProcessingDays != null ? Math.round(avgProcessingDays * 10) / 10.0 : null)
                .build();
    }

    // ========== 평균 계산 헬퍼 ==========

    /**
     * 일평균 민원 수 계산
     */
    private Double calculateAvgDailyCount(long totalCount) {
        LocalDateTime oldestDate = inquiryRepository.findOldestCreatedAt();
        if (oldestDate == null) {
            return 0.0;
        }

        long daysBetween = ChronoUnit.DAYS.between(oldestDate, LocalDateTime.now()) + 1;
        if (daysBetween == 0) {
            return (double) totalCount;
        }

        double avg = (double) totalCount / daysBetween;
        return Math.round(avg * 100) / 100.0; // 소수점 2자리
    }

    /**
     * 월평균 민원 수 계산
     */
    private Double calculateAvgMonthlyCount(long totalCount) {
        LocalDateTime oldestDate = inquiryRepository.findOldestCreatedAt();
        if (oldestDate == null) {
            return 0.0;
        }

        long monthsBetween = ChronoUnit.MONTHS.between(oldestDate, LocalDateTime.now()) + 1;
        if (monthsBetween == 0) {
            return (double) totalCount;
        }

        double avg = (double) totalCount / monthsBetween;
        return Math.round(avg * 100) / 100.0; // 소수점 2자리
    }

    /**
     * 퍼센트 계산 (소수점 2자리)
     */
    private Double calculatePercentage(long count, long total) {
        if (total == 0) {
            return 0.0;
        }
        double percentage = ((double) count / total) * 100;
        return Math.round(percentage * 100) / 100.0;
    }
}