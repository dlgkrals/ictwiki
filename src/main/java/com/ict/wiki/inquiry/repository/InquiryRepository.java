package com.ict.wiki.inquiry.repository;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.domain.InquiryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /**
     * 전체 민원 조회 (최신순, fetch join)
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker ORDER BY i.createdAt DESC")
    List<Inquiry> findAllWithWorker();

    /**
     * 최근 N개 민원 (홈 페이지용)
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker ORDER BY i.createdAt DESC LIMIT :limit")
    List<Inquiry> findRecentInquiries(@Param("limit") int limit);

    /**
     * 상태별 조회
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE i.status = :status ORDER BY i.createdAt DESC")
    List<Inquiry> findByStatus(@Param("status") InquiryStatus status);

    /**
     * 유형별 조회
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE i.type = :type ORDER BY i.createdAt DESC")
    List<Inquiry> findByType(@Param("type") InquiryType type);

    /**
     * 작업자별 조회 (내가 담당한 민원)
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE i.worker.id = :workerId ORDER BY i.createdAt DESC")
    List<Inquiry> findByWorkerId(@Param("workerId") Long workerId);

    /**
     * 작업자별 + 상태별 조회
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE i.worker.id = :workerId AND i.status = :status ORDER BY i.createdAt DESC")
    List<Inquiry> findByWorkerIdAndStatus(@Param("workerId") Long workerId, @Param("status") InquiryStatus status);

    /**
     * 요청자로 검색
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE i.requester LIKE %:requester% ORDER BY i.createdAt DESC")
    List<Inquiry> findByRequester(@Param("requester") String requester);

    /**
     * 제목으로 검색
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE i.title LIKE %:keyword% ORDER BY i.createdAt DESC")
    List<Inquiry> searchByTitle(@Param("keyword") String keyword);

    /**
     * 제목 + 설명으로 검색
     */
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.worker WHERE (i.title LIKE %:keyword% OR i.description LIKE %:keyword%) ORDER BY i.createdAt DESC")
    List<Inquiry> searchByTitleOrDescription(@Param("keyword") String keyword);

    /**
     * 내가 완료한 민원 개수
     */
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.worker.id = :workerId AND i.status = 'COMPLETED'")
    long countCompletedByWorkerId(@Param("workerId") Long workerId);

    /**
     * 내가 진행 중인 민원 개수
     */
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.worker.id = :workerId AND i.status = 'IN_PROGRESS'")
    long countInProgressByWorkerId(@Param("workerId") Long workerId);

    /**
     * 배정 대기 중인 민원 개수
     */
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status = 'PENDING'")
    long countPendingInquiries();

    /**
     * 미배정 민원 조회 (작업자가 없는 것)
     */
    @Query("SELECT i FROM Inquiry i WHERE i.worker IS NULL ORDER BY i.createdAt DESC")
    List<Inquiry> findUnassignedInquiries();

    /**
     * 상태별 민원 개수
     */
    @Query("SELECT i.status, COUNT(i) FROM Inquiry i GROUP BY i.status")
    List<Object[]> countByStatus();

    /**
     * 유형별 민원 개수
     */
    @Query("SELECT i.type, COUNT(i) FROM Inquiry i GROUP BY i.type")
    List<Object[]> countByType();

    /**
     * 작업자별 완료 민원 개수 (통계용)
     */
    @Query("SELECT i.worker.name, COUNT(i) FROM Inquiry i WHERE i.status = 'COMPLETED' AND i.worker IS NOT NULL GROUP BY i.worker.name ORDER BY COUNT(i) DESC")
    List<Object[]> countCompletedByWorker();
}