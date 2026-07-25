package com.ict.wiki.rag.domain;

import com.ict.wiki.config.FloatVectorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * RAG 검색용 벡터 청크
 * - 완료된 민원 또는 매뉴얼 슬라이드 묶음을 임베딩해서 저장
 */
@Entity
@Table(name = "rag_chunks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RagChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 청크 소스 유형
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceType sourceType;

    /**
     * 원본 ID (inquiry.id 또는 매뉴얼 chunk 순번)
     */
    @Column
    private Long sourceId;

    /**
     * 임베딩에 사용한 원문 텍스트 (디버깅/재임베딩용)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 512차원 벡터 (pgvector vector 타입)
     * ex) [0.123, -0.456, ...]
     */
    @Column(nullable = false, columnDefinition = "vector(512)")
    @Type(FloatVectorType.class)
    private float[] embedding;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SourceType {
        INQUIRY,
        MANUAL_USER,
        MANUAL_MAINTENANCE
    }
}