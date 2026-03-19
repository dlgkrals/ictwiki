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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStatsService {

    private final InquiryRepository inquiryRepository;

    // ========== 시간별 통계 ==========

    public long getDailyCount(LocalDate date) {
        return inquiryRepository.countByDate(date);
    }

    public long getMonthlyCount(int year, int month) {
        return inquiryRepository.countByYearAndMonth(year, month);
    }

    public long getYearlyCount(int year) {
        return inquiryRepository.countByYear(year);
    }

    public List<InquiryStatsResponse> getDailyStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<Object[]> results = inquiryRepository.countByDateRange(start, end);
        long totalCount = results.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        return results.stream()
                .map(result -> InquiryStatsResponse.builder()
                        .label(result[0].toString())
                        .count(((Number) result[1]).longValue())
                        .percentage(calculatePercentage(((Number) result[1]).longValue(), totalCount))
                        .build())
                .collect(Collectors.toList());
    }

    public List<InquiryStatsResponse> getMonthlyStatsInYear(int year) {
        List<Object[]> results = inquiryRepository.countByMonthsInYear(year);
        long totalCount = results.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
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

    // ========== 월별 탭 주차별 통계 ==========

    public List<InquiryStatsResponse> getWeeklyStatsInMonth(int year, int month) {
        List<Object[]> results = inquiryRepository.countByWeekInMonth(year, month);
        long totalCount = results.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        return results.stream()
                .map(result -> {
                    int week = ((Number) result[0]).intValue();
                    long count = ((Number) result[1]).longValue();
                    return InquiryStatsResponse.builder()
                            .label(week + "주차")
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 주차 범위 계산 헬퍼 ==========

    private LocalDateTime[] getWeekRange(int year, int week) {
        LocalDate weekStart = LocalDate.now()
                .with(IsoFields.WEEK_BASED_YEAR, year)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        return new LocalDateTime[]{weekStart.atStartOfDay(), weekEnd.atTime(23, 59, 59)};
    }

    // ========== 분류 통계 (연/월/주 공통) ==========

    public List<InquiryStatsResponse> getTypeStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByType,
                () -> inquiryRepository.countByTypeAndYear(year),
                () -> inquiryRepository.countByTypeAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, week); return inquiryRepository.countByTypeBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((InquiryType) r[0]).getDescription());
    }

    public List<InquiryStatsResponse> getMethodStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByMethod,
                () -> inquiryRepository.countByMethodAndYear(year),
                () -> inquiryRepository.countByMethodAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, week); return inquiryRepository.countByMethodBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((InquiryMethod) r[0]).getDescription());
    }

    public List<InquiryStatsResponse> getBuildingStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByBuilding,
                () -> inquiryRepository.countByBuildingAndYear(year),
                () -> inquiryRepository.countByBuildingAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, week); return inquiryRepository.countByBuildingBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((Building) r[0]).getDisplayName());
    }

    public List<InquiryStatsResponse> getStatusStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByStatus,
                () -> inquiryRepository.countByStatusAndYear(year),
                () -> inquiryRepository.countByStatusAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, week); return inquiryRepository.countByStatusBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((InquiryStatus) r[0]).getDescription());
    }

    // ========== 종합 대시보드 통계 ==========

    public InquiryDashboardStatsResponse getDashboardStats(Integer year) {
        long totalCount = (year == null)
                ? inquiryRepository.countAll()
                : inquiryRepository.countAllByYear(year);

        Map<String, Long> statusCounts = ((year == null)
                ? inquiryRepository.countByStatus()
                : inquiryRepository.countByStatusAndYear(year)).stream()
                .collect(Collectors.toMap(
                        result -> ((InquiryStatus) result[0]).getDescription(),
                        result -> ((Number) result[1]).longValue()
                ));

        return InquiryDashboardStatsResponse.builder()
                .totalCount(totalCount)
                .statusCounts(statusCounts)
                .typeCounts(getTypeStats(year, null, null))
                .methodCounts(getMethodStats(year, null, null))
                .buildingCounts(getBuildingStats(year, null, null))
                .avgDailyCount(calculateAvgDailyCount(totalCount))
                .avgWeeklyCount(calculateAvgWeeklyCount(totalCount))
                .avgMonthlyCount(calculateAvgMonthlyCount(totalCount))
                .build();
    }

    // ========== 헬퍼 ==========

    private List<Object[]> resolveResults(
            java.util.function.Supplier<List<Object[]>> allFn,
            java.util.function.Supplier<List<Object[]>> yearFn,
            java.util.function.Supplier<List<Object[]>> monthFn,
            java.util.function.Supplier<List<Object[]>> weekFn,
            Integer year, Integer month, Integer week) {
        if (year == null) return allFn.get();
        if (week != null) return weekFn.get();
        if (month != null) return monthFn.get();
        return yearFn.get();
    }

    private List<InquiryStatsResponse> toStatsResponse(
            List<Object[]> results,
            java.util.function.Function<Object[], String> labelFn) {
        long totalCount = results.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        return results.stream()
                .map(result -> {
                    long count = ((Number) result[1]).longValue();
                    return InquiryStatsResponse.builder()
                            .label(labelFn.apply(result))
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Long calculateAvgDailyCount(long totalCount) {
        LocalDateTime oldest = inquiryRepository.findOldestCreatedAt();
        if (oldest == null) return 0L;
        long days = ChronoUnit.DAYS.between(oldest, LocalDateTime.now()) + 1;
        return days == 0 ? totalCount : Math.round((double) totalCount / days);
    }

    private Long calculateAvgWeeklyCount(long totalCount) {
        LocalDateTime oldest = inquiryRepository.findOldestCreatedAt();
        if (oldest == null) return 0L;
        long weeks = ChronoUnit.WEEKS.between(oldest, LocalDateTime.now()) + 1;
        return weeks == 0 ? totalCount : Math.round((double) totalCount / weeks);
    }

    private Long calculateAvgMonthlyCount(long totalCount) {
        LocalDateTime oldest = inquiryRepository.findOldestCreatedAt();
        if (oldest == null) return 0L;
        long months = ChronoUnit.MONTHS.between(oldest, LocalDateTime.now()) + 1;
        return months == 0 ? totalCount : Math.round((double) totalCount / months);
    }

    private Double calculatePercentage(long count, long total) {
        if (total == 0) return 0.0;
        return Math.round(((double) count / total) * 10000) / 100.0;
    }
}