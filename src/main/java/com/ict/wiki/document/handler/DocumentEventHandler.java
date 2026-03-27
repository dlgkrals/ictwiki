package com.ict.wiki.document.handler;

import com.ict.wiki.document.events.*;
import com.ict.wiki.document.service.DocumentHistoryService;
import com.ict.wiki.document.service.DocumentLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentEventHandler {

    private final DocumentHistoryService historyService;
    private final DocumentLinkService linkService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDocumentCreated(DocumentCreatedEvent event) {
        log.info("=== 문서 생성 이벤트 처리 시작 - documentId: {}, title: {} ===",
                event.getDocumentId(), event.getTitle());
        try {
            historyService.createInitialHistory(event);
            linkService.processDocumentLinks(event);
            log.info("=== 문서 생성 이벤트 처리 완료 - documentId: {} ===", event.getDocumentId());
        } catch (Exception e) {
            log.error("문서 생성 이벤트 처리 실패 - documentId: {}", event.getDocumentId(), e);
            throw e;
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDocumentUpdated(DocumentUpdatedEvent event) {
        log.info("=== 문서 수정 이벤트 처리 시작 - documentId: {} ===", event.getDocumentId());
        try {
            historyService.createUpdateHistory(event);
            linkService.updateDocumentLinks(event);
            log.info("=== 문서 수정 이벤트 처리 완료 - documentId: {} ===", event.getDocumentId());
        } catch (Exception e) {
            log.error("문서 수정 이벤트 처리 실패 - documentId: {}", event.getDocumentId(), e);
            throw e;
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleDocumentPermanentDeleted(DocumentDeletedEvent event) {
        if (!event.isPermanent()) return;
        linkService.deleteAllDocumentLinks(event.getDocumentId());
        historyService.deleteHistoriesByDocumentId(event.getDocumentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDocumentSoftDeleted(DocumentDeletedEvent event) {
        if (event.isPermanent()) return;
        linkService.deleteAllDocumentLinks(event.getDocumentId());
    }
}