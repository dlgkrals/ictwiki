package com.ict.wiki.rag.handler;

import com.ict.wiki.inquiry.events.InquiryCompletedEvent;
import com.ict.wiki.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * RAG 이벤트 통합 처리기
 *
 * <p>민원 관련 이벤트를 수신하여 임베딩 저장을 처리합니다.</p>
 *
 * <h3>처리하는 이벤트:</h3>
 * <ul>
 *   <li>{@link InquiryCompletedEvent} - 민원 완료 시 → 임베딩 저장</li>
 * </ul>
 *
 * <p>@Async: OpenAI API 호출(네트워크 I/O)을 별도 스레드에서 처리하여
 * 민원 완료 API 응답 지연을 방지합니다.</p>
 *
 * <p>@TransactionalEventListener(AFTER_COMMIT): 트랜잭션 커밋 완료 후에만 실행하여
 * 비동기 스레드가 커밋 전 데이터를 읽는 문제를 방지합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEventHandler {

    private final RagService ragService;

    /**
     * 민원 완료 이벤트 처리
     *
     * <p>처리 내용:</p>
     * <ul>
     *   <li>완료된 민원을 임베딩하여 rag_chunks 테이블에 저장</li>
     * </ul>
     *
     * @param event 민원 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInquiryCompleted(InquiryCompletedEvent event) {
        log.info("=== 민원 완료 이벤트 처리 시작 - inquiryId: {} ===", event.getInquiryId());

        try {
            ragService.saveInquiryEmbedding(event.getInquiry());

            log.info("=== 민원 완료 이벤트 처리 완료 - inquiryId: {} ===", event.getInquiryId());

        } catch (Exception e) {
            log.error("민원 완료 이벤트 처리 실패 - inquiryId: {}", event.getInquiryId(), e);
            // 임베딩 실패는 민원 완료에 영향을 주지 않음 (이미 커밋된 상태)
        }
    }
}