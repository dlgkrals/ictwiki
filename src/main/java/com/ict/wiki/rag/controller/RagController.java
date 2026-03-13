package com.ict.wiki.rag.controller;

import com.ict.wiki.rag.service.SimilarCaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * RAG 유사 민원 API
 * POST /api/rag/ask           - 자유 질문 검색 (Postman 테스트용)
 * GET  /api/rag/similar/{id}  - 민원별 유사 사례 조회 (전구 버튼)
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final SimilarCaseService similarCaseService;

    /**
     * 자유 질문 검색 (저장 없음)
     * POST /api/rag/ask
     */
    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SimilarCaseService.SimilarCaseResult> ask(
            @Valid @RequestBody AskRequest request) {
        log.info("RAG 질문 - '{}'", request.question());
        return ResponseEntity.ok(similarCaseService.ask(request.question()));
    }

    /**
     * 민원별 유사 사례 조회 (전구 버튼)
     * GET /api/rag/similar/{inquiryId}
     * - 저장된 결과 있으면 바로 반환
     * - 없으면 생성 후 반환
     */
    @GetMapping("/similar/{inquiryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SimilarCaseService.SimilarCaseResult> getSimilarCases(
            @PathVariable Long inquiryId) {
        log.info("유사 사례 조회 - inquiryId: {}", inquiryId);
        return ResponseEntity.ok(similarCaseService.getOrCreate(inquiryId));
    }

    public record AskRequest(
            @NotBlank(message = "질문을 입력하세요")
            @Size(max = 500, message = "질문은 500자 이하로 입력하세요")
            String question
    ) {}
}