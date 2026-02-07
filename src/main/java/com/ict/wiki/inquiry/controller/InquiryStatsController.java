package com.ict.wiki.inquiry.controller;

import com.ict.wiki.inquiry.dto.response.InquiryDashboardStatsResponse;
import com.ict.wiki.inquiry.dto.response.InquiryStatsResponse;
import com.ict.wiki.inquiry.service.InquiryStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 민원 통계 API
 */
@Slf4j
@RestController
@RequestMapping("/api/inquiries/stats")
@RequiredArgsConstructor
public class InquiryStatsController {

    private final InquiryStatsService statsService;

    // ========== 시간별 통계 ==========

    /**
     * 일별 민원 건수
     * GET /api/inquiries/stats/daily?date=2025-02-06
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        long count = statsService.getDailyCount(date);

        return ResponseEntity.ok(Map.of(
                "date", date.toString(),
                "count", count
        ));
    }

    /**
     * 월별 민원 건수
     * GET /api/inquiries/stats/monthly?year=2025&month=2
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStats(
            @RequestParam int year,
            @RequestParam int month) {

        long count = statsService.getMonthlyCount(year, month);

        return ResponseEntity.ok(Map.of(
                "year", year,
                "month", month,
                "count", count
        ));
    }

    /**
     * 연별 민원 건수
     * GET /api/inquiries/stats/yearly?year=2025
     */
    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlyStats(@RequestParam int year) {
        long count = statsService.getYearlyCount(year);

        return ResponseEntity.ok(Map.of(
                "year", year,
                "count", count
        ));
    }

    /**
     * 기간별 일일 통계
     * GET /api/inquiries/stats/daily-range?startDate=2025-02-01&endDate=2025-02-28
     */
    @GetMapping("/daily-range")
    public ResponseEntity<List<InquiryStatsResponse>> getDailyRangeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<InquiryStatsResponse> stats = statsService.getDailyStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * 특정 연도의 월별 통계
     * GET /api/inquiries/stats/monthly-in-year?year=2025
     */
    @GetMapping("/monthly-in-year")
    public ResponseEntity<List<InquiryStatsResponse>> getMonthlyStatsInYear(@RequestParam int year) {
        List<InquiryStatsResponse> stats = statsService.getMonthlyStatsInYear(year);
        return ResponseEntity.ok(stats);
    }

    // ========== 유형/방식/건물별 통계 ==========

    /**
     * 유형별 통계 (PC, 네트워크 등)
     * GET /api/inquiries/stats/by-type
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<InquiryStatsResponse>> getTypeStats() {
        List<InquiryStatsResponse> stats = statsService.getTypeStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 처리방식별 통계 (원격/방문)
     * GET /api/inquiries/stats/by-method
     */
    @GetMapping("/by-method")
    public ResponseEntity<List<InquiryStatsResponse>> getMethodStats() {
        List<InquiryStatsResponse> stats = statsService.getMethodStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 건물별 통계
     * GET /api/inquiries/stats/by-building
     */
    @GetMapping("/by-building")
    public ResponseEntity<List<InquiryStatsResponse>> getBuildingStats() {
        List<InquiryStatsResponse> stats = statsService.getBuildingStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 상태별 통계
     * GET /api/inquiries/stats/by-status
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<InquiryStatsResponse>> getStatusStats() {
        List<InquiryStatsResponse> stats = statsService.getStatusStats();
        return ResponseEntity.ok(stats);
    }

    // ========== 종합 대시보드 ==========

    /**
     * 대시보드용 종합 통계
     * GET /api/inquiries/stats/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<InquiryDashboardStatsResponse> getDashboardStats() {
        InquiryDashboardStatsResponse stats = statsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}