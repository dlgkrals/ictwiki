package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Schedule;
import com.ict.wiki.timetable.dto.request.ScheduleExcelImportRequest;
import com.ict.wiki.timetable.dto.request.ScheduleImportRequest;
import com.ict.wiki.timetable.service.ScheduleExcelImportService;
import com.ict.wiki.timetable.service.ScheduleImportService;
import com.ict.wiki.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/schedules/import")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class ScheduleImportController {

    private final ScheduleImportService scheduleImportService;
    private final ScheduleExcelImportService scheduleExcelImportService;
    private final ScheduleService scheduleService;

    @PostMapping("/application")
    public ResponseEntity<Map<String, Object>> importFromApplication(
            @RequestBody List<ScheduleImportRequest> rows,
            @RequestParam(required = false) String semester) {
        String target = semester != null ? semester : getCurrentSemester();
        List<Schedule> schedules = scheduleImportService.importFromApplication(rows, target);
        return ResponseEntity.ok(Map.of(
                "message", schedules.size() + "건의 수업이 업로드되었습니다. 강의실을 배정해주세요.",
                "count", schedules.size(),
                "semester", target
        ));
    }

    @PostMapping("/timetable")
    public ResponseEntity<Map<String, Object>> importFromTimetable(
            @RequestBody List<ScheduleExcelImportRequest> rows,
            @RequestParam(required = false) String semester) {
        String target = semester != null ? semester : getCurrentSemester();
        List<Schedule> schedules = scheduleExcelImportService.importFromTimetableExcel(rows, target);
        return ResponseEntity.ok(Map.of(
                "message", schedules.size() + "건의 시간표가 등록되었습니다.",
                "count", schedules.size(),
                "semester", target
        ));
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return month <= 6 ? year + "-1" : year + "-2";
    }
}