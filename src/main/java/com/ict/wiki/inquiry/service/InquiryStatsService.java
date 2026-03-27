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
                            .label(month + "월 " + week + "주")
                            .count(count)
                            .percentage(calculatePercentage(count, totalCount))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== 주차 범위 계산 헬퍼 ==========

    private LocalDateTime[] getWeekRange(int year, int month, int week) {
        int startDay = switch (week) {
            case 1 -> 1;
            case 2 -> 8;
            case 3 -> 15;
            default -> 22;
        };
        int endDay = switch (week) {
            case 1 -> 7;
            case 2 -> 14;
            case 3 -> 21;
            default -> LocalDate.of(year, month, 1).lengthOfMonth();
        };
        LocalDateTime start = LocalDate.of(year, month, startDay).atStartOfDay();
        LocalDateTime end = (week == 4
                && year == LocalDate.now().getYear()
                && month == LocalDate.now().getMonthValue())
                ? LocalDateTime.now()
                : LocalDate.of(year, month, endDay).atTime(23, 59, 59);
        return new LocalDateTime[]{start, end};
    }

    // ========== 분류 통계 (연/월/주 공통) ==========

    public List<InquiryStatsResponse> getTypeStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByType,
                () -> inquiryRepository.countByTypeAndYear(year),
                () -> inquiryRepository.countByTypeAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, month, week); return inquiryRepository.countByTypeBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((InquiryType) r[0]).getDescription());
    }

    public List<InquiryStatsResponse> getMethodStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByMethod,
                () -> inquiryRepository.countByMethodAndYear(year),
                () -> inquiryRepository.countByMethodAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, month, week); return inquiryRepository.countByMethodBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((InquiryMethod) r[0]).getDescription());
    }

    public List<InquiryStatsResponse> getBuildingStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByBuilding,
                () -> inquiryRepository.countByBuildingAndYear(year),
                () -> inquiryRepository.countByBuildingAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, month, week); return inquiryRepository.countByBuildingBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((Building) r[0]).getDisplayName());
    }

    public List<InquiryStatsResponse> getStatusStats(Integer year, Integer month, Integer week) {
        List<Object[]> results = resolveResults(
                inquiryRepository::countByStatus,
                () -> inquiryRepository.countByStatusAndYear(year),
                () -> inquiryRepository.countByStatusAndYearMonth(year, month),
                () -> { LocalDateTime[] r = getWeekRange(year, month, week); return inquiryRepository.countByStatusBetween(r[0], r[1]); },
                year, month, week
        );
        return toStatsResponse(results, r -> ((InquiryStatus) r[0]).getDescription());
    }

    // ========== 종합 대시보드 통계 ==========

    public InquiryDashboardStatsResponse getDashboardStats(Integer year, Integer month, Integer week) {
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

        // 당주 모드
        if (year != null && month != null && week != null) {
            LocalDateTime[] weekRange = getWeekRange(year, month, week);
            long currentWeekCount = inquiryRepository.countByDateRange(weekRange[0], weekRange[1])
                    .stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
            long currentMonthCount = inquiryRepository.countByYearAndMonth(year, month);
            return InquiryDashboardStatsResponse.builder()
                    .totalCount(totalCount)
                    .statusCounts(statusCounts)
                    .typeCounts(getTypeStats(year, month, week))
                    .methodCounts(getMethodStats(year, month, week))
                    .buildingCounts(getBuildingStats(year, month, week))
                    .avgDailyCount(calculateAvgDailyCount(currentWeekCount, year, month, week))
                    .avgWeeklyCount(null)
                    .avgMonthlyCount(null)
                    .currentMonthCount(currentMonthCount)
                    .currentWeekCount(currentWeekCount)
                    .build();
        }

        // 당월 모드
        if (year != null && month != null) {
            long currentMonthCount = inquiryRepository.countByYearAndMonth(year, month);
            return InquiryDashboardStatsResponse.builder()
                    .totalCount(totalCount)
                    .statusCounts(statusCounts)
                    .typeCounts(getTypeStats(year, null, null))
                    .methodCounts(getMethodStats(year, null, null))
                    .buildingCounts(getBuildingStats(year, null, null))
                    .avgDailyCount(calculateAvgDailyCount(currentMonthCount, year, month, null))
                    .avgWeeklyCount(calculateAvgWeeklyCount(currentMonthCount, year, month))
                    .avgMonthlyCount(null)
                    .currentMonthCount(currentMonthCount)
                    .currentWeekCount(null)
                    .build();
        }

        // 연도/전체 모드
        return InquiryDashboardStatsResponse.builder()
                .totalCount(totalCount)
                .statusCounts(statusCounts)
                .typeCounts(getTypeStats(year, null, null))
                .methodCounts(getMethodStats(year, null, null))
                .buildingCounts(getBuildingStats(year, null, null))
                .avgDailyCount(calculateAvgDailyCount(totalCount, year, null, null))
                .avgWeeklyCount(calculateAvgWeeklyCount(totalCount, year, null))
                .avgMonthlyCount(calculateAvgMonthlyCount(totalCount, year))
                .currentMonthCount(null)
                .currentWeekCount(null)
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

    private LocalDateTime[] getYearRange(Integer year) {
        LocalDateTime start = (year == null)
                ? inquiryRepository.findOldestCreatedAt()
                : inquiryRepository.findOldestCreatedAtByYear(year);

        LocalDateTime end = (year == null || year == LocalDate.now().getYear())
                ? LocalDateTime.now()
                : LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        return new LocalDateTime[]{start, end};
    }

    private LocalDateTime[] getMonthRange(int year, int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = (year == LocalDate.now().getYear() && month == LocalDate.now().getMonthValue())
                ? LocalDateTime.now()
                : LocalDate.of(year, month, 1)
                .withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth())
                .atTime(23, 59, 59);
        return new LocalDateTime[]{start, end};
    }

    // month != null → 당월 모드, month == null → 연도/전체 모드
    private Long calculateAvgDailyCount(long count, Integer year, Integer month, Integer week) {
        LocalDateTime[] range = (week != null)
                ? getWeekRange(year, month, week)
                : (month != null)
                ? getMonthRange(year, month)
                : getYearRange(year);
        if (range[0] == null) return 0L;
        long days = ChronoUnit.DAYS.between(range[0], range[1]) + 1;
        return days == 0 ? count : Math.round((double) count / days);
    }

    private Long calculateAvgWeeklyCount(long count, Integer year, Integer month) {
        if (month != null) {
            int currentDay = (year == LocalDate.now().getYear() && month == LocalDate.now().getMonthValue())
                    ? LocalDate.now().getDayOfMonth()
                    : LocalDate.of(year, month, 1).lengthOfMonth();
            long weeks = currentDay < 8 ? 1 : currentDay < 15 ? 2 : currentDay < 22 ? 3 : 4;
            return Math.round((double) count / weeks);
        }
        LocalDateTime[] range = getYearRange(year);
        if (range[0] == null) return 0L;
        long days = ChronoUnit.DAYS.between(range[0], range[1]) + 1;
        long weeks = (long) Math.ceil((double) days / 7);
        return weeks == 0 ? count : Math.round((double) count / weeks);
    }

    private Long calculateAvgMonthlyCount(long count, Integer year) {
        LocalDateTime[] range = getYearRange(year);
        if (range[0] == null) return 0L;
        long months = ChronoUnit.MONTHS.between(range[0], range[1]) + 1;
        return months == 0 ? count : Math.round((double) count / months);
    }


    private Double calculatePercentage(long count, long total) {
        if (total == 0) return 0.0;
        return Math.round(((double) count / total) * 10000) / 100.0;
    }
}