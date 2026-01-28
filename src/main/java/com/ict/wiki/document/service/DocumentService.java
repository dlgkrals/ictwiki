package com.ict.wiki.document.service;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.document.events.DocumentCreatedEvent;
import com.ict.wiki.document.events.DocumentDeletedEvent;
import com.ict.wiki.document.events.DocumentUpdatedEvent;
import com.ict.wiki.document.repository.DocumentRepository;
import com.ict.wiki.login.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 위키 문서 서비스 (이벤트 기반)
 * - 문서 CRUD만 담당
 * - 링크/이력 관리는 이벤트로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 문서 생성
     */
    @Transactional
    public Document createDocument(String title, String content, User author) {
        // 제목 중복 체크
        if (documentRepository.existsByTitle(title)) {
            throw new IllegalArgumentException("이미 존재하는 제목입니다: " + title);
        }

        // 문서 생성
        Document document = Document.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("문서 생성 완료 - Title: {}, Author: {}", title, author.getEmail());

        // 🎯 이벤트 발행 (링크/이력은 이벤트 핸들러가 처리)
        eventPublisher.publishEvent(new DocumentCreatedEvent(savedDocument));

        return savedDocument;
    }

    /**
     * 문서 조회 (ID)
     */
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + id));
    }

    /**
     * 문서 조회 (제목)
     */
    public Document getDocumentByTitle(String title) {
        return documentRepository.findByTitleAndNotDeleted(title)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + title));
    }

    /**
     * 문서 조회 + 조회수 증가
     */
    @Transactional
    public Document getDocumentAndIncrementView(Long id) {
        Document document = getDocument(id);

        // 삭제된 문서는 조회수 증가 안 함
        if (!document.getDeleted()) {
            documentRepository.incrementViewCount(id);
            document.incrementViewCount();
            log.debug("조회수 증가 - Document ID: {}, New Count: {}", id, document.getViewCount());
        }

        return document;
    }

    /**
     * 문서 수정
     */
    @Transactional
    public Document updateDocument(Long id, String title, String content, User editor, String editReason) {
        Document document = getDocument(id);

        // 삭제된 문서는 수정 불가
        if (document.getDeleted()) {
            throw new IllegalArgumentException("삭제된 문서는 수정할 수 없습니다");
        }

        // 제목 중복 체크 (자기 자신 제외)
        if (!document.getTitle().equals(title) && documentRepository.existsByTitle(title)) {
            throw new IllegalArgumentException("이미 존재하는 제목입니다: " + title);
        }

        // 수정 전 상태 저장 (이벤트용)
        String oldTitle = document.getTitle();
        String oldContent = document.getContent();
        Integer oldVersion = document.getVersion();

        // 문서 수정
        document.update(title, content, editor);
        Document updatedDocument = documentRepository.save(document);

        log.info("문서 수정 완료 - ID: {}, Title: {}, Editor: {}, Version: {}",
                id, title, editor.getEmail(), updatedDocument.getVersion());

        // 🎯 이벤트 발행 (링크/이력은 이벤트 핸들러가 처리)
        eventPublisher.publishEvent(new DocumentUpdatedEvent(
                updatedDocument, oldTitle, oldContent, oldVersion, editor, editReason));

        return updatedDocument;
    }

    /**
     * 문서 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteDocument(Long id, User user) {
        Document document = getDocument(id);

        if (document.getDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 문서입니다");
        }

        String title = document.getTitle();

        // 소프트 삭제
        document.delete();
        documentRepository.save(document);

        log.info("문서 삭제 완료 - ID: {}, Title: {}, Deleted By: {}",
                id, title, user.getEmail());

        // 🎯 이벤트 발행 (링크 정리는 이벤트 핸들러가 처리)
        eventPublisher.publishEvent(new DocumentDeletedEvent(id, title, user));
    }

    /**
     * 문서 복원
     */
    @Transactional
    public void restoreDocument(Long id, User user) {
        Document document = getDocument(id);

        if (!document.getDeleted()) {
            throw new IllegalArgumentException("삭제되지 않은 문서입니다");
        }

        document.restore();
        documentRepository.save(document);

        log.info("문서 복원 완료 - ID: {}, Title: {}, Restored By: {}",
                id, document.getTitle(), user.getEmail());
    }

    /**
     * 문서 영구 삭제 (관리자 전용)
     */
    @Transactional
    public void permanentDeleteDocument(Long id, User admin) {
        Document document = getDocument(id);
        String title = document.getTitle();

        // 이벤트 먼저 발행 (링크 정리)
        eventPublisher.publishEvent(new DocumentDeletedEvent(id, title, admin));

        // 문서 삭제
        documentRepository.delete(document);

        log.warn("문서 영구 삭제 - ID: {}, Title: {}, Deleted By: {}",
                id, title, admin.getEmail());
    }

    /**
     * 모든 문서 조회 (삭제된 것 제외)
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAllNotDeleted();
    }

    /**
     * 제목으로 검색
     */
    public List<Document> searchByTitle(String keyword) {
        return documentRepository.searchByTitle(keyword);
    }

    /**
     * 제목 + 내용으로 검색
     */
    public List<Document> searchByTitleOrContent(String keyword) {
        return documentRepository.searchByTitleOrContent(keyword);
    }

    /**
     * 인기 문서 조회 (조회수 상위)
     */
    public List<Document> getPopularDocuments(int limit) {
        return documentRepository.findTopByViewCount(limit);
    }

    /**
     * 최근 생성 문서
     */
    public List<Document> getRecentDocuments(int limit) {
        return documentRepository.findRecentDocuments(limit);
    }

    /**
     * 최근 수정 문서
     */
    public List<Document> getRecentlyUpdatedDocuments(int limit) {
        return documentRepository.findRecentlyUpdatedDocuments(limit);
    }

    /**
     * 사용자가 작성한 문서
     */
    public List<Document> getDocumentsByAuthor(Long authorId) {
        return documentRepository.findByAuthorId(authorId);
    }

    /**
     * 삭제된 문서 조회 (관리자용)
     */
    public List<Document> getDeletedDocuments() {
        return documentRepository.findAllDeleted();
    }

    /**
     * 특정 버전으로 복원
     */
    @Transactional
    public Document restoreToVersion(Long documentId, Integer version, User editor) {
        // 이 메서드는 DocumentHistoryService와 협력 필요
        // 별도로 구현하거나 Facade 패턴 사용
        throw new UnsupportedOperationException("버전 복원은 별도 구현 필요");
    }
}