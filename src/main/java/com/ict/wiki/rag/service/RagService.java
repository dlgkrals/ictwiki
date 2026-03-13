package com.ict.wiki.rag.service;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryLocation;
import com.ict.wiki.rag.domain.RagChunk;
import com.ict.wiki.rag.repository.RagChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 벡터 저장 및 유사도 검색 서비스
 *
 * <p>임베딩 저장 흐름:</p>
 * <ol>
 *   <li>InquiryService.complete() → 이벤트 발행</li>
 *   <li>RagEventHandler.handleInquiryCompleted() → 이벤트 수신 (AFTER_COMMIT + @Async)</li>
 *   <li>RagService.saveInquiryEmbedding() → 실제 임베딩 저장</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagService {

    private final RagChunkRepository ragChunkRepository;
    private final EmbeddingService embeddingService;

    private static final double SIMILARITY_THRESHOLD = 0.7;
    private static final int TOP_K = 5;

    // ===== 임베딩 저장 =====

    /**
     * 완료된 민원을 임베딩하여 저장
     * RagEventHandler.handleInquiryCompleted() 에서 호출
     *
     * @param inquiry 완료된 민원 (solution 포함 보장)
     */
    @Transactional
    public void saveInquiryEmbedding(Inquiry inquiry) {
        // solution 없는 민원은 임베딩하지 않음 (품질 보장)
        if (inquiry.getSolution() == null || inquiry.getSolution().isBlank()) {
            log.debug("solution 없어 임베딩 스킵 - 민원 ID: {}", inquiry.getId());
            return;
        }

        if (ragChunkRepository.findByInquiryId(inquiry.getId()).isPresent()) {
            log.debug("이미 임베딩된 민원 스킵 - ID: {}", inquiry.getId());
            return;
        }

        String content = buildInquiryContent(inquiry);
        float[] vector = embeddingService.embed(content); // 실패 시 RagException 발생

        RagChunk chunk = RagChunk.builder()
                .sourceType(RagChunk.SourceType.INQUIRY)
                .sourceId(inquiry.getId())
                .content(content)
                .embedding(vector)
                .build();

        ragChunkRepository.save(chunk);
        log.info("민원 임베딩 저장 완료 - 민원 ID: {}", inquiry.getId());
    }

    // ===== 유사 청크 검색 =====

    /**
     * 자연어 질문에 대한 유사 청크 검색
     *
     * @param question 사용자 질문
     * @return 유사도 순 정렬된 청크 목록 (상위 TOP_K개, 임계값 이상만)
     */
    public List<ScoredChunk> search(String question) {
        float[] queryVector = embeddingService.embed(question);
        List<RagChunk> allChunks = ragChunkRepository.findAll();

        return allChunks.stream()
                .map(chunk -> new ScoredChunk(chunk, cosineSimilarity(queryVector, chunk.getEmbedding())))
                .filter(sc -> sc.score() >= SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(TOP_K)
                .collect(Collectors.toList());
    }

    // ===== 임베딩 원문 구성 =====

    private String buildInquiryContent(Inquiry inquiry) {
        String location = inquiry.getLocations().isEmpty()
                ? "위치 미상"
                : inquiry.getLocations().stream()
                .map(InquiryLocation::getFormatted)
                .collect(Collectors.joining(", "));

        return String.format("유형: %s | 위치: %s | 문제: %s | 해결: %s",
                inquiry.getType().getDescription(),
                location,
                inquiry.getDescription(),
                inquiry.getSolution());
    }

    // ===== 코사인 유사도 계산 =====

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ===== 내부 레코드 =====

    public record ScoredChunk(RagChunk chunk, double score) {}
}