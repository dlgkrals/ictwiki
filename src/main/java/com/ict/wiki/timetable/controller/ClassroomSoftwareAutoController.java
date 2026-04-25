package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.service.ClassroomSoftwareAutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/classrooms/software-auto")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class ClassroomSoftwareAutoController {

    private final ClassroomSoftwareAutoService classroomSoftwareAutoService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> autoAssign(
            @RequestParam(required = false) String semester) {

        String target = semester != null ? semester : getCurrentSemester();
        ClassroomSoftwareAutoService.AutoResult result = classroomSoftwareAutoService.autoAssign(target);

        return ResponseEntity.ok(Map.of(
                "semester", target,
                "classroomCount", result.classroomCount(),
                "assignedCount", result.assignedCount(),
                "failureCount", result.failureCount()
        ));
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) return year + "-1";
        return year + "-2";
    }
}
