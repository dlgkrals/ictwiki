package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Classroom;
import com.ict.wiki.timetable.domain.ClassroomSoftware;
import com.ict.wiki.timetable.dto.request.ClassroomCreateRequest;
import com.ict.wiki.timetable.dto.response.ClassroomResponse;
import com.ict.wiki.timetable.dto.response.ClassroomSoftwareResponse;
import com.ict.wiki.timetable.dto.response.ClassroomWithSoftwaresResponse;
import com.ict.wiki.timetable.service.ClassroomService;
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
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> getAllClassrooms() {
        List<ClassroomResponse> response = classroomService.getAllClassrooms().stream()
                .map(ClassroomResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getClassroom(@PathVariable Long id) {
        return ResponseEntity.ok(ClassroomResponse.from(classroomService.findById(id)));
    }

    @GetMapping("/available")
    public ResponseEntity<List<ClassroomResponse>> getAvailableClassrooms(
            @RequestParam(required = false) List<Long> softwareIds) {
        List<ClassroomResponse> response = classroomService.findAvailableClassrooms(softwareIds).stream()
                .map(ClassroomResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ClassroomResponse> createClassroom(
            @Valid @RequestBody ClassroomCreateRequest request) {
        Classroom classroom = classroomService.createClassroom(
                request.getRoomNumber(), request.getFloor(), request.getGrade());
        return ResponseEntity.ok(ClassroomResponse.from(classroom));
    }

    @PatchMapping("/{id}/grade")
    public ResponseEntity<ClassroomResponse> updateGrade(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Classroom classroom = classroomService.updateGrade(id, body.get("grade"));
        return ResponseEntity.ok(ClassroomResponse.from(classroom));
    }

    @GetMapping("/{id}/softwares")
    public ResponseEntity<List<ClassroomSoftwareResponse>> getSoftwares(@PathVariable Long id) {
        List<ClassroomSoftwareResponse> response = classroomService.getSoftwares(id).stream()
                .map(ClassroomSoftwareResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/softwares")
    public ResponseEntity<ClassroomSoftwareResponse> addSoftware(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        ClassroomSoftware cs = classroomService.addSoftware(id, body.get("softwareId"));
        return ResponseEntity.ok(ClassroomSoftwareResponse.from(cs));
    }

    @DeleteMapping("/{id}/softwares/{softwareId}")
    public ResponseEntity<Map<String, String>> removeSoftware(
            @PathVariable Long id,
            @PathVariable Long softwareId) {
        classroomService.removeSoftware(id, softwareId);
        return ResponseEntity.ok(Map.of("message", "소프트웨어가 삭제되었습니다."));
    }

    @GetMapping("/softwares")
    public ResponseEntity<List<ClassroomWithSoftwaresResponse>> getAllClassroomsWithSoftwares() {
        return ResponseEntity.ok(classroomService.getAllClassroomsWithSoftwares());
    }
}