package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Schedule;
import com.ict.wiki.timetable.dto.request.ClassroomAssignRequest;
import com.ict.wiki.timetable.dto.response.ScheduleResponse;
import com.ict.wiki.timetable.service.ScheduleExcelImportService;
import com.ict.wiki.timetable.service.ScheduleImportService;
import com.ict.wiki.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 신청서 엑셀 업로드 → 미배정 수업 목록 저장 (현재 학기 자동 적용)
     * POST /api/schedules/import/application
     */
    @PostMapping("/application")
    public ResponseEntity<Map<String, Object>> importFromApplication(
            @RequestParam("file") MultipartFile file) {
        String semester = getCurrentSemester();
        List<Schedule> schedules = scheduleImportService.importFromApplication(file, semester);
        return ResponseEntity.ok(Map.of(
                "message", schedules.size() + "건의 수업이 업로드되었습니다. 강의실을 배정해주세요.",
                "count", schedules.size(),
                "semester", semester
        ));
    }

    /**
     * 완성된 시간표 엑셀 업로드 → 강의실 배정 시트 파싱 후 일괄 저장
     * POST /api/schedules/import/timetable
     */
    @PostMapping("/timetable")
    public ResponseEntity<Map<String, Object>> importFromTimetable(
            @RequestParam("file") MultipartFile file) {
        String semester = getCurrentSemester();
        List<Schedule> schedules = scheduleExcelImportService.importFromTimetableExcel(file, semester);
        return ResponseEntity.ok(Map.of(
                "message", schedules.size() + "건의 시간표가 등록되었습니다.",
                "count", schedules.size(),
                "semester", semester
        ));
    }

    /**
     * 미배정 수업 목록 조회 (현재 학기 자동 적용)
     * GET /api/schedules/import/unassigned
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<ScheduleResponse>> getUnassigned() {
        String semester = getCurrentSemester();
        List<ScheduleResponse> response = scheduleService.getUnassigned(semester).stream()
                .map(ScheduleResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 미배정 수업에 강의실 배정
     * PATCH /api/schedules/import/{id}/assign
     */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ScheduleResponse> assignClassroom(
            @PathVariable Long id,
            @RequestBody ClassroomAssignRequest request) {
        Schedule schedule = scheduleService.assignClassroom(id, request.getClassroomId());
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) return year + "-1";
        return year + "-2";
    }
}