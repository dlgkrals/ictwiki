package com.ict.wiki.document.repository;

import com.ict.wiki.document.domain.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {

    /**
     * 특정 문서의 모든 수정 이력 조회 (최신순)
     */
    @Query("SELECT h FROM DocumentHistory h WHERE h.document.id = :documentId ORDER BY h.version DESC")
    List<DocumentHistory> findByDocumentIdOrderByVersionDesc(@Param("documentId") Long documentId);

    /**
     * 특정 문서의 특정 버전 조회
     */
    @Query("SELECT h FROM DocumentHistory h WHERE h.document.id = :documentId AND h.version = :version")
    Optional<DocumentHistory> findByDocumentIdAndVersion(@Param("documentId") Long documentId, @Param("version") Integer version);

    /**
     * 특정 문서의 최신 N개 이력 조회
     */
    @Query("SELECT h FROM DocumentHistory h WHERE h.document.id = :documentId ORDER BY h.version DESC LIMIT :limit")
    List<DocumentHistory> findRecentHistoriesByDocumentId(@Param("documentId") Long documentId, @Param("limit") int limit);

    /**
     * 특정 사용자가 수정한 이력 조회
     */
    @Query("SELECT h FROM DocumentHistory h WHERE h.editor.id = :editorId ORDER BY h.editedAt DESC")
    List<DocumentHistory> findByEditorId(@Param("editorId") Long editorId);

    /**
     * 특정 문서의 이력 개수 조회
     */
    @Query("SELECT COUNT(h) FROM DocumentHistory h WHERE h.document.id = :documentId")
    long countByDocumentId(@Param("documentId") Long documentId);

    /**
     * 전체 시스템의 최근 수정 이력 N개 (모든 문서 포함)
     */
    @Query("SELECT h FROM DocumentHistory h ORDER BY h.editedAt DESC LIMIT :limit")
    List<DocumentHistory> findRecentHistories(@Param("limit") int limit);
}