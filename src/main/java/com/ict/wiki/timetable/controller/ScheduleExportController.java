package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.service.ScheduleExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 교무처용 시간표 엑셀 내보내기 API
 */
@Slf4j
@RestController
@RequestMapping("/api/schedules/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class ScheduleExportController {

    private final ScheduleExportService scheduleExportService;

    /**
     * 시간표 엑셀 다운로드
     * semester 미입력 시 현재 학기 자동 적용
     * GET /api/schedules/export?semester=2026-1
     */
    @GetMapping
    public ResponseEntity<byte[]> exportSchedule(
            @RequestParam(required = false) String semester) throws IOException {

        String target = semester != null ? semester : getCurrentSemester();
        byte[] excelBytes = scheduleExportService.exportToExcel(target);

        String filename = URLEncoder.encode(target + "_시간표.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) return year + "-1";
        return year + "-2";
    }
}