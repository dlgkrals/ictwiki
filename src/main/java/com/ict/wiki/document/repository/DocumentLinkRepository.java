package com.ict.wiki.document.repository;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.document.domain.DocumentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentLinkRepository extends JpaRepository<DocumentLink, Long> {

    /**
     * 특정 문서가 링크하는 모든 문서 조회 (나가는 링크)
     */
    @Query("SELECT dl FROM DocumentLink dl WHERE dl.sourceDocument.id = :documentId")
    List<DocumentLink> findBySourceDocumentId(@Param("documentId") Long documentId);

    /**
     * 특정 문서를 링크하는 모든 문서 조회 (들어오는 링크, 역참조)
     */
    @Query("SELECT dl FROM DocumentLink dl WHERE dl.targetDocument.id = :documentId")
    List<DocumentLink> findByTargetDocumentId(@Param("documentId") Long documentId);

    /**
     * 두 문서 간 링크 존재 여부 확인
     */
    @Query("SELECT dl FROM DocumentLink dl WHERE dl.sourceDocument.id = :sourceId AND dl.targetDocument.id = :targetId")
    Optional<DocumentLink> findBySourceAndTarget(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    /**
     * 특정 문서의 모든 링크 삭제 (문서 삭제 시)
     */
    @Modifying
    @Query("DELETE FROM DocumentLink dl WHERE dl.sourceDocument.id = :documentId OR dl.targetDocument.id = :documentId")
    void deleteAllByDocumentId(@Param("documentId") Long documentId);

    /**
     * 특정 문서가 링크하는 문서 수 (나가는 링크 수)
     */
    @Query("SELECT COUNT(dl) FROM DocumentLink dl WHERE dl.sourceDocument.id = :documentId")
    long countBySourceDocumentId(@Param("documentId") Long documentId);

    /**
     * 특정 문서를 링크하는 문서 수 (들어오는 링크 수, 역참조 수)
     */
    @Query("SELECT COUNT(dl) FROM DocumentLink dl WHERE dl.targetDocument.id = :documentId")
    long countByTargetDocumentId(@Param("documentId") Long documentId);

    /**
     * 가장 많이 참조되는 문서 N개 (인기 문서)
     */
    @Query("SELECT dl.targetDocument, COUNT(dl) as linkCount FROM DocumentLink dl " +
            "GROUP BY dl.targetDocument ORDER BY linkCount DESC LIMIT :limit")
    List<Object[]> findMostLinkedDocuments(@Param("limit") int limit);

    /**
     * 특정 문서의 나가는 링크를 모두 삭제
     */
    @Modifying
    @Query("DELETE FROM DocumentLink dl WHERE dl.sourceDocument.id = :documentId")
    void deleteBySourceDocumentId(@Param("documentId") Long documentId);
}