package com.ict.wiki.rag.service;

import com.ict.wiki.exception.code.RagErrorCode;
import com.ict.wiki.exception.custom.RagException;
import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import com.ict.wiki.rag.domain.InquirySimilarCase;
import com.ict.wiki.rag.domain.RagChunk;
import com.ict.wiki.rag.repository.InquirySimilarCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 유사 사례 검색 + 요약 + 저장/조회 서비스
 *
 * <p>흐름:</p>
 * <ol>
 *   <li>전구 클릭 → GET /api/rag/similar/{inquiryId}</li>
 *   <li>저장된 결과 있으면 → 바로 반환 (OpenAI 호출 없음)</li>
 *   <li>없으면 → 벡터 검색 + LLM 요약 → DB 저장 → 반환</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimilarCaseService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.prompt.summary}")
    private String summaryPrompt;

    private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini";
    private static final int MAX_TOKENS = 300;

    private final InquirySimilarCaseRepository similarCaseRepository;
    private final InquiryRepository inquiryRepository;
    private final RagService ragService;
    private final RestTemplate restTemplate;

    // ===== 조회 (전구 버튼) =====

    /**
     * 유사 사례 조회 - 있으면 반환, 없으면 생성 후 반환
     */
    @Transactional
    public SimilarCaseResult getOrCreate(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> RagException.of(RagErrorCode.SIMILAR_CASE_NOT_FOUND));

        Optional<InquirySimilarCase> existing = similarCaseRepository.findByInquiry(inquiry);
        if (existing.isPresent()) {
            log.debug("유사 사례 히트 - inquiryId: {}", inquiryId);
            return toResult(existing.get());
        }

        log.info("유사 사례 신규 생성 - inquiryId: {}", inquiryId);
        return createAndSave(inquiry);
    }

    // ===== 직접 검색 (Postman 테스트용) =====

    /**
     * 질문 문자열로 직접 검색 (저장 없음)
     */
    public SimilarCaseResult ask(String question) {
        List<RagService.ScoredChunk> chunks = ragService.search(question);
        log.info("유사 청크 검색 완료 - 질문: '{}', {}건", question, chunks.size());

        List<ReferenceItem> references = toReferenceItems(chunks);
        String summary = references.isEmpty() ? "관련 사례 없음" : summarize(references);

        return new SimilarCaseResult(summary, chunks.size(), references);
    }

    // ===== 내부 로직 =====

    private SimilarCaseResult createAndSave(Inquiry inquiry) {
        String question = buildQuestion(inquiry);
        List<RagService.ScoredChunk> chunks = ragService.search(question);

        List<ReferenceItem> references = toReferenceItems(chunks);
        String summary = references.isEmpty() ? "관련 사례 없음" : summarize(references);

        String relatedIds = references.stream()
                .map(ref -> String.valueOf(ref.inquiryId()))
                .collect(Collectors.joining(","));

        InquirySimilarCase similarCase = InquirySimilarCase.of(
                inquiry, summary, relatedIds, references.size()
        );
        similarCaseRepository.save(similarCase);
        log.info("유사 사례 저장 완료 - inquiryId: {}, {}건", inquiry.getId(), references.size());

        return new SimilarCaseResult(summary, references.size(), references);
    }

    /**
     * 저장된 ID로 원본 민원 최신 데이터 조회
     * - 원본 수정돼도 항상 최신 반영
     */
    private SimilarCaseResult toResult(InquirySimilarCase similarCase) {
        if (similarCase.getRelatedInquiryIds().isBlank()) {
            return new SimilarCaseResult(similarCase.getSummary(), 0, List.of());
        }

        List<Long> ids = Arrays.stream(similarCase.getRelatedInquiryIds().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<ReferenceItem> references = inquiryRepository.findAllById(ids).stream()
                .map(related -> new ReferenceItem(
                        related.getId(),
                        related.getType().getDescription(),
                        related.getLocations().isEmpty() ? "위치 미상"
                                : related.getLocations().get(0).getFormatted(),
                        related.getDescription(),
                        related.getSolution()
                ))
                .collect(Collectors.toList());

        return new SimilarCaseResult(similarCase.getSummary(), similarCase.getReferenceCount(), references);
    }

    /**
     * ScoredChunk → ReferenceItem 변환
     */
    private List<ReferenceItem> toReferenceItems(List<RagService.ScoredChunk> chunks) {
        return chunks.stream()
                .filter(sc -> sc.chunk().getSourceType() == RagChunk.SourceType.INQUIRY)
                .map(sc -> {
                    String content = sc.chunk().getContent();
                    return new ReferenceItem(
                            sc.chunk().getSourceId(),
                            extractField(content, "유형"),
                            extractField(content, "위치"),
                            extractField(content, "문제"),
                            extractField(content, "해결")
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * "유형: X | 위치: Y | 문제: Z | 해결: W" 형식에서 필드 추출
     */
    private String extractField(String content, String field) {
        String prefix = field + ": ";
        int start = content.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = content.indexOf(" | ", start);
        return end == -1 ? content.substring(start).trim() : content.substring(start, end).trim();
    }

    /**
     * solution 목록 → LLM bullet 요약
     */
    private String summarize(List<ReferenceItem> references) {
        String solutionList = references.stream()
                .map(ReferenceItem::solution)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.joining("\n- ", "- ", ""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", MAX_TOKENS,
                "messages", List.of(
                        Map.of("role", "system", "content", summaryPrompt),
                        Map.of("role", "user", "content", solutionList)
                )
        );

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    CHAT_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class
            );
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("LLM 요약 실패", e);
            throw RagException.of(RagErrorCode.CHAT_FAILED);
        }
    }

    /**
     * 민원 내용을 벡터 검색용 질문 형식으로 변환
     */
    private String buildQuestion(Inquiry inquiry) {
        String location = inquiry.getLocations().isEmpty() ? "위치 미상"
                : inquiry.getLocations().get(0).getFormatted();
        return String.format("유형: %s | 위치: %s | 문제: %s",
                inquiry.getType().getDescription(),
                location,
                inquiry.getDescription());
    }

    // ===== 응답 레코드 =====

    public record ReferenceItem(
            Long inquiryId,
            String type,
            String location,
            String problem,
            String solution
    ) {}

    public record SimilarCaseResult(
            String summary,
            int referenceCount,
            List<ReferenceItem> references
    ) {}
}