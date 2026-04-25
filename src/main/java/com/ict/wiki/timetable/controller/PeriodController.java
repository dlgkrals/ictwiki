package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Period;
import com.ict.wiki.timetable.domain.PeriodType;
import com.ict.wiki.timetable.service.PeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class PeriodController {

    private final PeriodService periodService;

    /**
     * 전체 교시 목록 (주/야 그룹)
     * GET /api/periods
     * → { "DAY": [...], "NIGHT": [...] }
     */
    @GetMapping
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAllPeriods() {
        Map<String, List<Map<String, Object>>> result = periodService.getAllPeriods().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getType().name(),
                        Collectors.mapping(this::toMap, Collectors.toList())
                ));
        return ResponseEntity.ok(result);
    }

    /**
     * 주/야 구분 교시 목록
     * GET /api/periods/by-type?type=DAY
     * GET /api/periods/by-type?type=NIGHT
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<Map<String, Object>>> getPeriodsByType(
            @RequestParam PeriodType type) {
        List<Map<String, Object>> result = periodService.getPeriodsByType(type).stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(Period p) {
        return Map.of(
                "periodNumber", p.getPeriodNumber(),
                "type", p.getType().name(),
                "label", p.getType().getLabel() + p.getPeriodNumber() + "교시",
                "startTime", p.getStartTime().toString(),
                "endTime", p.getEndTime().toString()
        );
    }
}