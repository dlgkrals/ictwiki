package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.dto.response.ScheduleExportResponse;
import com.ict.wiki.timetable.service.ScheduleExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/schedules/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class ScheduleExportController {

    private final ScheduleExportService scheduleExportService;

    @GetMapping
    public ResponseEntity<ScheduleExportResponse> exportSchedule(
            @RequestParam(required = false) String semester) {

        String target = semester != null ? semester : getCurrentSemester();
        return ResponseEntity.ok(scheduleExportService.getExportData(target));
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return month <= 6 ? year + "-1" : year + "-2";
    }
}