package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.dto.response.SoftwareMatchFailureResponse;
import com.ict.wiki.timetable.service.SoftwareMatchFailureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms/software-auto/failures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class SoftwareMatchFailureController {

    private final SoftwareMatchFailureService failureService;

    /**
     * 학기별 매칭 실패 목록 조회
     * GET /api/classrooms/software-auto/failures?semester=2026-1
     */
    @GetMapping
    public ResponseEntity<List<SoftwareMatchFailureResponse>> getFailures(
            @RequestParam String semester) {
        return ResponseEntity.ok(failureService.getFailures(semester));
    }

    /**
     * 단건 해결 처리
     * PATCH /api/classrooms/software-auto/failures/{id}/resolve
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<SoftwareMatchFailureResponse> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(failureService.resolve(id));
    }
}
