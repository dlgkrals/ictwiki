package com.ict.wiki.document.handler;

import com.ict.wiki.document.events.*;
import com.ict.wiki.document.service.DocumentHistoryService;
import com.ict.wiki.document.service.DocumentLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문서 이벤트 통합 처리기
 *
 * <p>모든 문서 관련 이벤트를 한 곳에서 처리하여 이벤트 흐름을 명확하게 관리합니다.</p>
 *
 * <h3>처리하는 이벤트:</h3>
 * <ul>
 *   <li>{@link DocumentCreatedEvent} - 문서 생성 시</li>
 *   <li>{@link DocumentUpdatedEvent} - 문서 수정 시</li>
 *   <li>{@link DocumentDeletedEvent} - 문서 삭제 시</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentEventHandler {

    private final DocumentHistoryService historyService;
    private final DocumentLinkService linkService;

    /**
     * 문서 생성 이벤트 처리
     *
     * <p>처리 순서:</p>
     * <ol>
     *   <li>초기 이력 생성 (DocumentHistoryService)</li>
     *   <li>문서 내 링크 파싱 및 저장 (DocumentLinkService)</li>
     * </ol>
     *
     * @param event 문서 생성 이벤트
     */
    @EventListener
    @Transactional
    public void handleDocumentCreated(DocumentCreatedEvent event) {
        log.info("=== 문서 생성 이벤트 처리 시작 - documentId: {}, title: {} ===",
                event.getDocumentId(), event.getTitle());

        try {
            // 1. 초기 이력 생성
            log.debug("1단계: 초기 이력 생성");
            historyService.createInitialHistory(event);
            log.debug("1단계 완료: 이력 생성 성공");

            // 2. 링크 파싱 및 저장
            log.debug("2단계: 링크 파싱 및 저장");
            linkService.processDocumentLinks(event);
            log.debug("2단계 완료: 링크 처리 성공");

            log.info("=== 문서 생성 이벤트 처리 완료 - documentId: {} ===",
                    event.getDocumentId());

        } catch (Exception e) {
            log.error("문서 생성 이벤트 처리 실패 - documentId: {}", event.getDocumentId(), e);
            throw e; // 트랜잭션 롤백
        }
    }

    /**
     * 문서 수정 이벤트 처리
     *
     * <p>처리 순서:</p>
     * <ol>
     *   <li>수정 전 상태를 이력에 저장 (DocumentHistoryService)</li>
     *   <li>기존 링크 삭제 및 새 링크 생성 (DocumentLinkService)</li>
     * </ol>
     *
     * @param event 문서 수정 이벤트
     */
    @EventListener
    @Transactional
    public void handleDocumentUpdated(DocumentUpdatedEvent event) {
        log.info("=== 문서 수정 이벤트 처리 시작 - documentId: {}, version: {} ===",
                event.getDocumentId(), event.getOldVersion());

        try {
            // 1. 수정 이력 저장 (수정 전 상태)
            log.debug("1단계: 수정 전 상태를 이력에 저장");
            historyService.createUpdateHistory(event);
            log.debug("1단계 완료: 이력 저장 성공");

            // 2. 링크 재생성 (기존 링크 삭제 후 새로 파싱)
            log.debug("2단계: 링크 재생성");
            linkService.updateDocumentLinks(event);
            log.debug("2단계 완료: 링크 업데이트 성공");

            log.info("=== 문서 수정 이벤트 처리 완료 - documentId: {} ===",
                    event.getDocumentId());

        } catch (Exception e) {
            log.error("문서 수정 이벤트 처리 실패 - documentId: {}", event.getDocumentId(), e);
            throw e;
        }
    }

    /**
     * 문서 삭제 이벤트 처리
     *
     * <p>처리 내용:</p>
     * <ul>
     *   <li>문서와 관련된 모든 링크 삭제 (DocumentLinkService)</li>
     * </ul>
     *
     * <p>참고: 이력은 감사 목적으로 유지됩니다.</p>
     *
     * @param event 문서 삭제 이벤트
     */
    @EventListener
    @Transactional
    public void handleDocumentDeleted(DocumentDeletedEvent event) {
        log.info("=== 문서 삭제 이벤트 처리 시작 - documentId: {} ===",
                event.getDocumentId());

        try {
            // 관련 링크 정리
            log.debug("링크 정리 시작");
            linkService.deleteAllDocumentLinks(event.getDocumentId());
            log.debug("링크 정리 완료");

            log.info("=== 문서 삭제 이벤트 처리 완료 - documentId: {} ===",
                    event.getDocumentId());

        } catch (Exception e) {
            log.error("문서 삭제 이벤트 처리 실패 - documentId: {}", event.getDocumentId(), e);
            throw e;
        }
    }
}