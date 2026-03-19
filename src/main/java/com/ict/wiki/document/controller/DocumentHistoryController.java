package com.ict.wiki.document.controller;

import com.ict.wiki.document.domain.DocumentHistory;
import com.ict.wiki.document.dto.response.DocumentHistoryResponse;
import com.ict.wiki.document.service.DocumentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 수정 이력 API
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentHistoryController {

    private final DocumentHistoryService historyService;

    /**
     * 특정 문서의 전체 수정 이력 조회 (최신순)
     * GET /api/documents/{id}/histories
     */
    @GetMapping("/{id}/histories")
    public ResponseEntity<List<DocumentHistoryResponse>> getHistories(@PathVariable Long id) {
        List<DocumentHistory> histories = historyService.getDocumentHistories(id);
        List<DocumentHistoryResponse> response = histories.stream()
                .map(DocumentHistoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 문서의 특정 버전 조회
     * GET /api/documents/{id}/histories/{version}
     */
    @GetMapping("/{id}/histories/{version}")
    public ResponseEntity<DocumentHistoryResponse> getHistoryByVersion(
            @PathVariable Long id,
            @PathVariable Integer version) {
        DocumentHistory history = historyService.getDocumentHistory(id, version);
        return ResponseEntity.ok(DocumentHistoryResponse.from(history));
    }

    /**
     * 특정 문서의 최근 N개 이력 조회
     * GET /api/documents/{id}/histories/recent?limit=5
     */
    @GetMapping("/{id}/histories/recent")
    public ResponseEntity<List<DocumentHistoryResponse>> getRecentHistories(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        List<DocumentHistory> histories = historyService.getRecentHistories(id, limit);
        List<DocumentHistoryResponse> response = histories.stream()
                .map(DocumentHistoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 문서의 이력 개수 조회
     * GET /api/documents/{id}/histories/count
     */
    @GetMapping("/{id}/histories/count")
    public ResponseEntity<Long> countHistories(@PathVariable Long id) {
        long count = historyService.countHistories(id);
        return ResponseEntity.ok(count);
    }
}