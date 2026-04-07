package com.ict.wiki.report.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.service.UserService;
import com.ict.wiki.report.service.DailyReportService;
import com.ict.wiki.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final UserService userService;

    @GetMapping("/daily")
    public ResponseEntity<byte[]> downloadDailyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws Exception {

        User user = userService.findById(userDetails.getId());
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        byte[] excelBytes = dailyReportService.generateReport(user, targetDate);

        String filename = targetDate.format(DateTimeFormatter.ofPattern("MM월dd일"))
                + "_" + user.getName() + "_업무일지.xls";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}