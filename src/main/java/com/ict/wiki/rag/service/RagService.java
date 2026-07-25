package com.ict.wiki.rag.service;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryLocation;
import com.ict.wiki.rag.domain.RagChunk;
import com.ict.wiki.rag.repository.RagChunkRepository;
import com.ict.wiki.rag.util.InquiryContentBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final float SIMILARITY_THRESHOLD = 0.7f;
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

        String content = InquiryContentBuilder.build(inquiry);
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
     * 자연어 질문에 대한 유사 청크 검색 (pgvector <=> 코사인 거리, DB 처리)
     *
     * @param question 사용자 질문
     * @return 유사도 순 정렬된 청크 목록 (상위 TOP_K개, 임계값 이상만)
     */
    public List<ScoredChunk> search(String question) {
        float[] queryVector = embeddingService.embed(question);
        String vectorStr = toVectorString(queryVector);

        return ragChunkRepository.findTopKByCosineDistance(vectorStr, TOP_K)
                .stream()
                .map(chunk -> {
                    float cosineDistance = cosineSimilarity(queryVector, chunk.getEmbedding());
                    return new ScoredChunk(chunk, cosineDistance);
                })
                .filter(sc -> sc.score() >= SIMILARITY_THRESHOLD)
                .collect(Collectors.toList());
    }

    // ===== 헬퍼 =====

    /** float[] → pgvector 문자열 "[0.1, -0.2, ...]" */
    private String toVectorString(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        return sb.append(']').toString();
    }

    /** DB에서 받은 청크의 코사인 유사도 계산 (임계값 필터용) */
    private float cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0f;
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    // ===== 내부 레코드 =====

    public record ScoredChunk(RagChunk chunk, float score) {}
}