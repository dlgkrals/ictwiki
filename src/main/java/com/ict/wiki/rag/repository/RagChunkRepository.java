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
     * pgvector <=> 코사인 거리로 상위 K개 유사 청크 검색 (DB 처리)
     * :queryVector 형식: "[0.1, -0.2, ...]"
     */
    @Query(value = """
            SELECT * FROM rag_chunks
            ORDER BY embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagChunk> findTopKByCosineDistance(
            @Param("queryVector") String queryVector,
            @Param("limit") int limit
    );


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