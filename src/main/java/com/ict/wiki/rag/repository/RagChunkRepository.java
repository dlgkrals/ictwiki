package com.ict.wiki.rag.repository;

import com.ict.wiki.rag.domain.RagChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RagChunkRepository extends JpaRepository<RagChunk, Long> {

    /**
     * 유사도 계산을 위한 전체 벡터 로드
     * (900건 × 512차원 × 4bytes ≈ 1.8MB, 메모리 부담 없음)
     */
    List<RagChunk> findAll();

    /**
     * 민원 ID로 기존 청크 조회 (중복 저장 방지)
     */
    @Query("SELECT r FROM RagChunk r WHERE r.sourceType = 'INQUIRY' AND r.sourceId = :inquiryId")
    Optional<RagChunk> findByInquiryId(@Param("inquiryId") Long inquiryId);

    /**
     * 매뉴얼 청크만 조회
     */
    List<RagChunk> findBySourceType(RagChunk.SourceType sourceType);
}