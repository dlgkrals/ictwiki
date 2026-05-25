package com.ict.wiki.report.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.service.UserService;
import com.ict.wiki.report.dto.response.DailyReportResponse;
import com.ict.wiki.report.service.DailyReportService;
import com.ict.wiki.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final UserService userService;

    @GetMapping("/daily")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userService.findById(userDetails.getId());
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return ResponseEntity.ok(dailyReportService.getReportData(user, targetDate));
    }
}