package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Classroom;
import com.ict.wiki.timetable.domain.Makeup;
import com.ict.wiki.timetable.dto.request.MakeupAvailableRequest;
import com.ict.wiki.timetable.dto.request.MakeupCreateRequest;
import com.ict.wiki.timetable.dto.response.ClassroomResponse;
import com.ict.wiki.timetable.dto.response.MakeupResponse;
import com.ict.wiki.timetable.service.MakeupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/makeups")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class MakeupController {

    private final MakeupService makeupService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MakeupResponse>> getMakeupsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        List<MakeupResponse> response = makeupService.getByMonth(year, month).stream()
                .map(MakeupResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MakeupResponse> getMakeup(@PathVariable Long id) {
        return ResponseEntity.ok(MakeupResponse.from(makeupService.findById(id)));
    }

    @PostMapping("/available-classrooms")
    public ResponseEntity<List<ClassroomResponse>> getAvailableClassrooms(
            @Valid @RequestBody MakeupAvailableRequest req) {
        List<Classroom> classrooms = makeupService.findAvailableClassrooms(
                req.getDate(), req.getStartTime(), req.getEndTime(), req.getSoftwareIds());
        List<ClassroomResponse> response = classrooms.stream()
                .map(ClassroomResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MakeupResponse> createMakeup(
            @Valid @RequestBody MakeupCreateRequest req) {
        Makeup makeup = makeupService.createMakeup(
                req.getClassroomId(),
                req.getDepartment(), req.getCourseName(), req.getProfessor(),
                req.getDate(), req.getStartTime(), req.getEndTime(),
                req.getSoftwareNote(), req.getPurpose(), req.getNote()
        );
        return ResponseEntity.ok(MakeupResponse.from(makeup));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<MakeupResponse>> createMakeups(
            @Valid @RequestBody List<MakeupCreateRequest> requests) {
        return ResponseEntity.ok(makeupService.createMakeups(requests));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMakeup(@PathVariable Long id) {
        makeupService.deleteMakeup(id);
        return ResponseEntity.ok(Map.of("message", "보강이 삭제되었습니다."));
    }
}