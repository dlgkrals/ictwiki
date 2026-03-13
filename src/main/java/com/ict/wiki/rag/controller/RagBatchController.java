package com.ict.wiki.rag.controller;

import com.ict.wiki.rag.service.RagBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * RAG 배치 작업 API
 * - 관리자 전용 (ADMIN, TA)
 */
@Slf4j
@RestController
@RequestMapping("/api/rag/batch")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TA')")
public class RagBatchController {

    private final RagBatchService ragBatchService;

    /**
     * 기존 완료 민원 전체 배치 임베딩
     * POST /api/rag/batch/embed
     *
     * 서비스 최초 도입 시 1회 실행
     * 이미 임베딩된 민원은 자동 스킵
     */
    @PostMapping("/embed")
    public ResponseEntity<Map<String, Object>> embedAll() {
        log.info("배치 임베딩 API 호출");

        RagBatchService.BatchResult result = ragBatchService.embedAllCompletedInquiries();

        return ResponseEntity.ok(Map.of(
                "total", result.total(),
                "success", result.success(),
                "skipped", result.skipped(),
                "failed", result.failed(),
                "message", String.format("배치 임베딩 완료 - 성공: %d, 스킵: %d, 실패: %d",
                        result.success(), result.skipped(), result.failed())
        ));
    }
}