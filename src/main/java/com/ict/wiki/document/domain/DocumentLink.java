package com.ict.wiki.document.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 문서 간 링크 관계
 * - [[문서제목]] 형태의 링크 추적
 * - 역참조 (어떤 문서가 이 문서를 참조하는지) 추적 가능
 */
@Entity
@Table(name = "document_links",
        indexes = {
                // 성능 최적화: "이 문서가 링크하는 문서들" 조회 속도 향상
                @Index(name = "idx_source_document", columnList = "source_document_id"),
                // 성능 최적화: "이 문서를 링크하는 문서들(역참조)" 조회 속도 향상
                @Index(name = "idx_target_document", columnList = "target_document_id")
        },
        // 데이터 무결성: 같은 문서 간 중복 링크 방지 (A→B 링크가 여러 개 생성되는 것 방지)
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"source_document_id", "target_document_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 링크를 포함하는 문서 (출발)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id", nullable = false)
    private Document sourceDocument;

    /**
     * 링크 대상 문서 (도착)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_document_id", nullable = false)
    private Document targetDocument;

    /**
     * 링크에 표시되는 텍스트
     * - [[표시텍스트|문서제목]]에서 "표시텍스트" 부분
     * - 없으면 문서 제목 사용
     */
    @Column(length = 200)
    private String displayText;

    // ========== 팩토리 메서드 ==========

    /**
     * 문서 링크 생성
     */
    public static DocumentLink of(Document source, Document target, String displayText) {
        return DocumentLink.builder()
                .sourceDocument(source)
                .targetDocument(target)
                .displayText(displayText)
                .build();
    }
}