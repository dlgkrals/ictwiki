package com.ict.wiki.document.service;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.document.domain.DocumentHistory;
import com.ict.wiki.document.events.DocumentCreatedEvent;
import com.ict.wiki.document.events.DocumentUpdatedEvent;
import com.ict.wiki.document.repository.DocumentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 문서 수정 이력 서비스 (이벤트 기반)
 * - 이벤트를 받아서 이력 저장
 * - 이력 조회 기능
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentHistoryService {

    private final DocumentHistoryRepository historyRepository;

    /**
     * 초기 이력 생성 (문서 생성 이벤트 처리용)
     */
    @Transactional
    public void createInitialHistory(DocumentCreatedEvent event) {
        Document document = event.getDocument();

        DocumentHistory history = DocumentHistory.builder()
                .document(document)
                .version(1)
                .title(event.getTitle())
                .content(event.getContent())
                .editor(event.getAuthor())
                .editReason("문서 생성")
                .build();

        historyRepository.save(history);
        log.debug("초기 이력 생성 완료 - DocumentId: {}, Version: 1", document.getId());
    }

    /**
     * 수정 이력 생성 (문서 수정 이벤트 처리용)
     */
    @Transactional
    public void createUpdateHistory(DocumentUpdatedEvent event) {
        Document document = event.getDocument();

        // 수정 전 상태를 이력에 저장
        DocumentHistory history = DocumentHistory.builder()
                .document(document)
                .version(event.getOldVersion())
                .title(event.getOldTitle())
                .content(event.getOldContent())
                .editor(event.getEditor())
                .editReason(event.getEditReason())
                .build();

        historyRepository.save(history);
        log.debug("수정 이력 생성 완료 - DocumentId: {}, Version: {}",
                document.getId(), event.getOldVersion());
    }

    /**
     * 특정 문서의 모든 수정 이력 조회 (최신순)
     */
    public List<DocumentHistory> getDocumentHistories(Long documentId) {
        return historyRepository.findByDocumentIdOrderByVersionDesc(documentId);
    }

    /**
     * 특정 문서의 특정 버전 조회
     */
    public DocumentHistory getDocumentHistory(Long documentId, Integer version) {
        return historyRepository.findByDocumentIdAndVersion(documentId, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 버전을 찾을 수 없습니다 - Document: " + documentId + ", Version: " + version));
    }

    /**
     * 특정 문서의 최근 N개 이력
     */
    public List<DocumentHistory> getRecentHistories(Long documentId, int limit) {
        return historyRepository.findRecentHistoriesByDocumentId(documentId, limit);
    }

    /**
     * 특정 사용자가 수정한 모든 이력
     */
    public List<DocumentHistory> getHistoriesByEditor(Long editorId) {
        return historyRepository.findByEditorId(editorId);
    }

    /**
     * 특정 문서의 이력 개수
     */
    public long countHistories(Long documentId) {
        return historyRepository.countByDocumentId(documentId);
    }

    /**
     * 전체 시스템의 최근 수정 이력 (모든 문서 포함)
     */
    public List<DocumentHistory> getRecentSystemHistories(int limit) {
        return historyRepository.findRecentHistories(limit);
    }

    /**
     * 두 버전 간 차이 비교
     */
    public VersionDiff compareVersions(Long documentId, Integer version1, Integer version2) {
        DocumentHistory history1 = getDocumentHistory(documentId, version1);
        DocumentHistory history2 = getDocumentHistory(documentId, version2);

        boolean titleChanged = !history1.getTitle().equals(history2.getTitle());
        boolean contentChanged = !history1.getContent().equals(history2.getContent());

        return new VersionDiff(
                version1, version2,
                history1, history2,
                titleChanged, contentChanged
        );
    }

    // ========== 내부 클래스 ==========

    /**
     * 버전 비교 결과
     */
    public static class VersionDiff {
        public final Integer version1;
        public final Integer version2;
        public final DocumentHistory history1;
        public final DocumentHistory history2;
        public final boolean titleChanged;
        public final boolean contentChanged;

        public VersionDiff(Integer version1, Integer version2,
                           DocumentHistory history1, DocumentHistory history2,
                           boolean titleChanged, boolean contentChanged) {
            this.version1 = version1;
            this.version2 = version2;
            this.history1 = history1;
            this.history2 = history2;
            this.titleChanged = titleChanged;
            this.contentChanged = contentChanged;
        }
    }
}