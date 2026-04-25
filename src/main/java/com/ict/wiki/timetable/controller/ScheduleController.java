package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Schedule;
import com.ict.wiki.timetable.dto.request.ScheduleCreateRequest;
import com.ict.wiki.timetable.dto.request.ScheduleUpdateRequest;
import com.ict.wiki.timetable.dto.response.ScheduleResponse;
import com.ict.wiki.timetable.service.ScheduleService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @RequestParam(required = false) String semester) {
        String target = semester != null ? semester : getCurrentSemester();
        List<ScheduleResponse> response = scheduleService.getBySemester(target).stream()
                .map(ScheduleResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByClassroom(
            @PathVariable Long classroomId,
            @RequestParam(required = false) String semester) {
        String target = semester != null ? semester : getCurrentSemester();
        List<ScheduleResponse> response = scheduleService
                .getBySemesterAndClassroom(target, classroomId).stream()
                .map(ScheduleResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(ScheduleResponse.from(scheduleService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody ScheduleCreateRequest req) {
        String semester = getCurrentSemester();
        Schedule schedule = scheduleService.createSchedule(
                semester, req.getClassroomId(),
                req.getDepartment(), req.getGrade(), req.getSection(),
                req.getCourseName(), req.getProfessor(),
                req.getDayOfWeek(), req.getPeriodType(),
                req.getPeriodStart(), req.getPeriodEnd(),
                req.getSoftwareNote(),
                req.getHasOption() != null && req.getHasOption(),
                req.getOptionNote(),
                req.getPriority()
        );
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    /**
     * PATCH /api/schedules/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleUpdateRequest req) {
        Schedule schedule = scheduleService.updateSchedule(id, req);
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(Map.of("message", "시간표가 삭제되었습니다."));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteBySemester(
            @RequestParam(required = false) String semester) {
        String target = semester != null ? semester : getCurrentSemester();
        scheduleService.deleteBySemester(target);
        return ResponseEntity.ok(Map.of("message", target + " 시간표가 전체 삭제되었습니다."));
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) return year + "-1";
        return year + "-2";
    }

    @GetMapping("/semesters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getSemesters() {
        return ResponseEntity.ok(scheduleService.getAllSemesters());
    }
}