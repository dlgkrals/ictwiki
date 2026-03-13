package com.ict.wiki.rag.service;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import com.ict.wiki.rag.repository.RagChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기존 완료 민원 배치 임베딩 서비스
 * - 서비스 최초 도입 시 1회 실행
 * - 재실행 시 기존 청크 전체 삭제 후 새로 임베딩 (새로고침)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagBatchService {

    private final InquiryRepository inquiryRepository;
    private final RagChunkRepository ragChunkRepository;
    private final RagService ragService;

    /**
     * 완료된 민원 전체 배치 임베딩
     * POST /api/rag/batch/embed 에서 호출
     * - 기존 청크 전체 삭제 후 재임베딩
     *
     * @return 처리 결과 요약
     */
    @Transactional
    public BatchResult embedAllCompletedInquiries() {
        List<Inquiry> completed = inquiryRepository.findByStatus(InquiryStatus.COMPLETED);
        int total = completed.size();

        // 기존 청크 전체 삭제 (새로고침)
        long existing = ragChunkRepository.count();
        if (existing > 0) {
            ragChunkRepository.deleteAll();
            log.info("기존 청크 {}건 삭제 완료", existing);
        }

        int success = 0;
        int skipped = 0;
        int failed = 0;

        log.info("배치 임베딩 시작 - 완료 민원 총 {}건", total);

        for (Inquiry inquiry : completed) {
            try {
                // solution 없는 민원 스킵 (임베딩 품질 보장)
                if (inquiry.getSolution() == null || inquiry.getSolution().isBlank()) {
                    skipped++;
                    log.debug("solution 없어 스킵 - 민원 ID: {}", inquiry.getId());
                    continue;
                }

                ragService.saveInquiryEmbedding(inquiry);
                success++;

                // OpenAI API rate limit 방지 (1건당 200ms 간격)
                Thread.sleep(200);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("배치 임베딩 중단 - 인터럽트 발생");
                break;
            } catch (Exception e) {
                failed++;
                log.error("임베딩 실패 - 민원 ID: {}", inquiry.getId(), e);
            }
        }

        log.info("배치 임베딩 완료 - 성공: {}, 스킵: {}, 실패: {}", success, skipped, failed);
        return new BatchResult(total, success, skipped, failed);
    }

    public record BatchResult(int total, int success, int skipped, int failed) {}
}