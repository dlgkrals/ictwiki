package com.ict.wiki.document.dto.response;

import com.ict.wiki.document.domain.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문서 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String title;
    private String content;
    private Long viewCount;
    private Integer version;
    private Boolean deleted;

    // 카테고리 정보
    private String categoryCode;
    private String categoryName;

    // 작성자 정보
    private Long authorId;
    private String authorName;
    private String authorEmail;

    // 최종 수정자 정보
    private Long lastEditorId;
    private String lastEditorName;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity → Response DTO 변환
     */
    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .viewCount(document.getViewCount())
                .version(document.getVersion())
                .deleted(document.getDeleted())
                .categoryCode(document.getCategory().name())           // ← 카테고리 코드
                .categoryName(document.getCategory().getDisplayName()) // ← 카테고리 디스플레이 네임
                .authorId(document.getAuthor().getId())
                .authorName(document.getAuthor().getName())
                .authorEmail(document.getAuthor().getEmail())
                .lastEditorId(document.getLastEditor() != null ?
                        document.getLastEditor().getId() : null)
                .lastEditorName(document.getLastEditor() != null ?
                        document.getLastEditor().getName() : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getModifiedAt())
                .build();
    }
}