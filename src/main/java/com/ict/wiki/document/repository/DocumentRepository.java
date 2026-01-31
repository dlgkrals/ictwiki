package com.ict.wiki.document.repository;

import com.ict.wiki.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * 제목으로 문서 조회 (대소문자 구분 없이)
     */
    Optional<Document> findByTitleIgnoreCase(String title);

    /**
     * 제목으로 문서 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT d FROM Document d WHERE d.title = :title AND d.deleted = false")
    Optional<Document> findByTitleAndNotDeleted(@Param("title") String title);

    /**
     * 제목 존재 여부 확인
     */
    boolean existsByTitle(String title);

    /**
     * 제목으로 검색 (LIKE, 삭제되지 않은 것만) - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.title LIKE %:keyword% AND d.deleted = false ORDER BY d.createdAt DESC")
    List<Document> searchByTitle(@Param("keyword") String keyword);

    /**
     * 제목 또는 내용으로 검색 (삭제되지 않은 것만) - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE (d.title LIKE %:keyword% OR d.content LIKE %:keyword%) AND d.deleted = false ORDER BY d.createdAt DESC")
    List<Document> searchByTitleOrContent(@Param("keyword") String keyword);

    /**
     * 삭제되지 않은 모든 문서 조회 (최신순) - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.deleted = false ORDER BY d.createdAt DESC")
    List<Document> findAllNotDeleted();

    /**
     * 특정 사용자가 작성한 문서 조회 - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.author.id = :authorId AND d.deleted = false ORDER BY d.createdAt DESC")
    List<Document> findByAuthorId(@Param("authorId") Long authorId);

    /**
     * 조회수 상위 N개 문서 - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.deleted = false ORDER BY d.viewCount DESC LIMIT :limit")
    List<Document> findTopByViewCount(@Param("limit") int limit);

    /**
     * 최근 생성된 N개 문서 - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.deleted = false ORDER BY d.createdAt DESC LIMIT :limit")
    List<Document> findRecentDocuments(@Param("limit") int limit);

    /**
     * 최근 수정된 N개 문서 - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.deleted = false ORDER BY d.modifiedAt DESC LIMIT :limit")
    List<Document> findRecentlyUpdatedDocuments(@Param("limit") int limit);

    /**
     * 조회수 증가 (벌크 연산)
     */
    @Modifying
    @Query("UPDATE Document d SET d.viewCount = d.viewCount + 1 WHERE d.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 삭제된 문서 조회 (관리자용) - fetch join 추가
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.deleted = true ORDER BY d.modifiedAt DESC")
    List<Document> findAllDeleted();

    /**
     * 단건 조회
     */
    @Query("SELECT d FROM Document d JOIN FETCH d.author WHERE d.id = :id")
    Optional<Document> findByIdWithAuthor(@Param("id") Long id);

    /**
     * 마지막 작업자 포함 조회
     */
    @Query("""
    SELECT d FROM Document d
    JOIN FETCH d.author
    LEFT JOIN FETCH d.lastEditor
    WHERE d.id = :id
""")
    Optional<Document> findByIdWithAuthorAndLastEditor(@Param("id") Long id);
}