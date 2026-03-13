package com.ict.wiki.rag.service;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import com.ict.wiki.rag.domain.RagChunk;
import com.ict.wiki.rag.repository.RagChunkRepository;
import com.ict.wiki.rag.util.InquiryContentBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 기존 완료 민원 배치 임베딩 서비스
 * - 서비스 최초 도입 시 1회 실행
 * - 재실행 시 기존 청크 전체 삭제 후 새로 임베딩 (새로고침)
 * - 50건씩 나눠서 OpenAI 배치 임베딩 → saveAll() 한 번에 INSERT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagBatchService {

    private static final int BATCH_SIZE = 50;

    private final InquiryRepository inquiryRepository;
    private final RagChunkRepository ragChunkRepository;
    private final EmbeddingService embeddingService;
    private final RagService ragService;

    @Transactional
    public BatchResult embedAllCompletedInquiries() {
        List<Inquiry> completed = inquiryRepository.findByStatus(InquiryStatus.COMPLETED);
        int total = completed.size();

        // 기존 청크 전체 삭제
        long existing = ragChunkRepository.count();
        if (existing > 0) {
            ragChunkRepository.deleteAllInBatch();
            log.info("기존 청크 {}건 삭제 완료", existing);
        }

        // solution 없는 민원 필터링
        List<Inquiry> targets = completed.stream()
                .filter(i -> i.getSolution() != null && !i.getSolution().isBlank())
                .toList();

        int skipped = total - targets.size();
        int success = 0;
        int failed = 0;

        log.info("배치 임베딩 시작 - 대상 {}건 (스킵 {}건)", targets.size(), skipped);

        // 50건씩 나눠서 처리
        for (int i = 0; i < targets.size(); i += BATCH_SIZE) {
            List<Inquiry> batch = targets.subList(i, Math.min(i + BATCH_SIZE, targets.size()));

            try {
                List<String> contents = batch.stream()
                        .map(InquiryContentBuilder::build)
                        .toList();

                List<float[]> vectors = embeddingService.embedBatch(contents);

                List<RagChunk> chunks = new ArrayList<>();
                for (int j = 0; j < batch.size(); j++) {
                    chunks.add(RagChunk.builder()
                            .sourceType(RagChunk.SourceType.INQUIRY)
                            .sourceId(batch.get(j).getId())
                            .content(contents.get(j))
                            .embedding(vectors.get(j))
                            .build());
                }

                ragChunkRepository.saveAll(chunks);
                success += chunks.size();
                log.info("배치 임베딩 저장 완료 - {}~{}번째 / 총 {}건",
                        i + 1, i + batch.size(), targets.size());

            } catch (Exception e) {
                failed += batch.size();
                log.error("배치 임베딩 실패 - {}~{}번째", i + 1, i + batch.size(), e);
            }
        }

        log.info("배치 임베딩 완료 - 성공: {}, 스킵: {}, 실패: {}", success, skipped, failed);
        return new BatchResult(total, success, skipped, failed);
    }

    public record BatchResult(int total, int success, int skipped, int failed) {}
}