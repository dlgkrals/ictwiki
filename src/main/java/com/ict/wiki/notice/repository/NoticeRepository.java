package com.ict.wiki.notice.repository;

import com.ict.wiki.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 전체 목록 (최신순)
     */
    @Query("SELECT n FROM Notice n ORDER BY n.createdAt DESC")
    List<Notice> findAllOrderByCreatedAtDesc();

    /**
     * 최근 N개 (홈 페이지용)
     */
    @Query("SELECT n FROM Notice n ORDER BY n.createdAt DESC LIMIT :limit")
    List<Notice> findRecentNotices(@Param("limit") int limit);

    /**
     * 제목 검색
     */
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% ORDER BY n.createdAt DESC")
    List<Notice> searchByTitle(@Param("keyword") String keyword);

    /**
     * 제목 + 내용 검색
     */
    @Query("SELECT n FROM Notice n WHERE (n.title LIKE %:keyword% OR n.content LIKE %:keyword%) ORDER BY n.createdAt DESC")
    List<Notice> search(@Param("keyword") String keyword);
}