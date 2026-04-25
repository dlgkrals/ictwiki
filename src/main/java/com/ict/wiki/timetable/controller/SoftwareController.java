package com.ict.wiki.timetable.controller;

import com.ict.wiki.timetable.domain.Software;
import com.ict.wiki.timetable.dto.request.SoftwareCreateRequest;
import com.ict.wiki.timetable.dto.response.SoftwareResponse;
import com.ict.wiki.timetable.service.SoftwareService;
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
@RequestMapping("/api/softwares")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class SoftwareController {

    private final SoftwareService softwareService;

    @GetMapping
    public ResponseEntity<List<SoftwareResponse>> getAllSoftwares() {
        List<SoftwareResponse> response = softwareService.getAllSoftwares().stream()
                .map(s -> SoftwareResponse.from(s, softwareService.fromJson(s.getAliases())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoftwareResponse> getSoftware(@PathVariable Long id) {
        Software s = softwareService.findById(id);
        return ResponseEntity.ok(SoftwareResponse.from(s, softwareService.fromJson(s.getAliases())));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SoftwareResponse>> searchSoftwares(@RequestParam String keyword) {
        List<SoftwareResponse> response = softwareService.search(keyword).stream()
                .map(s -> SoftwareResponse.from(s, softwareService.fromJson(s.getAliases())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SoftwareResponse> createSoftware(
            @Valid @RequestBody SoftwareCreateRequest request) {
        Software s = softwareService.createSoftware(
                request.getName(),
                request.getAliases(),
                request.getIsDefault());
        return ResponseEntity.ok(SoftwareResponse.from(s, softwareService.fromJson(s.getAliases())));
    }

    @PutMapping("/{id}/aliases")
    public ResponseEntity<SoftwareResponse> updateAliases(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> body) {
        Software s = softwareService.updateAliases(id, body.get("aliases"));
        return ResponseEntity.ok(SoftwareResponse.from(s, softwareService.fromJson(s.getAliases())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSoftware(@PathVariable Long id) {
        softwareService.deleteSoftware(id);
        return ResponseEntity.ok(Map.of("message", "소프트웨어가 삭제되었습니다."));
    }

    @PatchMapping("/{id}/version-sensitive")
    public ResponseEntity<SoftwareResponse> updateVersionSensitive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Software s = softwareService.updateVersionSensitive(id, body.get("versionSensitive"));
        return ResponseEntity.ok(SoftwareResponse.from(s, softwareService.fromJson(s.getAliases())));
    }
}