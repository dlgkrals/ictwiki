package com.ict.wiki.document.dto.response;

import com.ict.wiki.document.domain.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문서 목록 응답 (간단 버전)
 * - 목록 조회 시 사용 (내용 제외)
 */
@Getter
@Builder
@AllArgsConstructor
public class DocumentSummaryResponse {

    private Long id;
    private String title;
    private Long viewCount;
    private Integer version;

    // 카테고리 정보
    private String categoryCode;
    private String categoryName;

    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity → Summary DTO 변환
     */
    public static DocumentSummaryResponse from(Document document) {
        return DocumentSummaryResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .viewCount(document.getViewCount())
                .version(document.getVersion())
                .categoryCode(document.getCategory().name())           // ← 추가
                .categoryName(document.getCategory().getDisplayName()) // ← 추가
                .authorName(document.getAuthor().getName())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getModifiedAt())
                .build();
    }
}