package com.ict.wiki.notice.controller;

import com.ict.wiki.notice.domain.Notice;
import com.ict.wiki.notice.dto.request.NoticeCreateRequest;
import com.ict.wiki.notice.dto.request.NoticeUpdateRequest;
import com.ict.wiki.notice.dto.response.NoticeResponse;
import com.ict.wiki.notice.dto.response.NoticeSummaryResponse;
import com.ict.wiki.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공지사항 API
 */
@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ========== 생성 ==========

    /**
     * 공지사항 생성
     * POST /api/notices
     */
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeCreateRequest request) {

        Notice notice = noticeService.createNotice(
                request.getTitle(),
                request.getContent()
        );

        return ResponseEntity.ok(NoticeResponse.from(notice));
    }

    // ========== 조회 ==========

    /**
     * 공지사항 단건 조회
     * GET /api/notices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long id) {
        Notice notice = noticeService.findById(id);
        return ResponseEntity.ok(NoticeResponse.from(notice));
    }

    /**
     * 전체 목록 조회 (최신순)
     * GET /api/notices
     */
    @GetMapping
    public ResponseEntity<List<NoticeSummaryResponse>> getAllNotices() {
        List<Notice> notices = noticeService.findAll();

        List<NoticeSummaryResponse> response = notices.stream()
                .map(NoticeSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 최근 N개 조회 (홈용)
     * GET /api/notices/recent?limit=3
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NoticeSummaryResponse>> getRecentNotices(
            @RequestParam(defaultValue = "3") int limit) {

        List<Notice> notices = noticeService.findRecent(limit);

        List<NoticeSummaryResponse> response = notices.stream()
                .map(NoticeSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== 검색 ==========

    /**
     * 제목 + 내용 검색
     * GET /api/notices/search?keyword=시스템
     */
    @GetMapping("/search")
    public ResponseEntity<List<NoticeSummaryResponse>> searchNotices(
            @RequestParam String keyword) {

        List<Notice> notices = noticeService.search(keyword);

        List<NoticeSummaryResponse> response = notices.stream()
                .map(NoticeSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== 수정 ==========

    /**
     * 공지사항 수정
     * PUT /api/notices/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody NoticeUpdateRequest request) {

        Notice notice = noticeService.update(
                id,
                request.getTitle(),
                request.getContent()
        );

        return ResponseEntity.ok(NoticeResponse.from(notice));
    }

    // ========== 삭제 ==========

    /**
     * 공지사항 삭제
     * DELETE /api/notices/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotice(@PathVariable Long id) {
        noticeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "공지사항이 삭제되었습니다"));
    }
}