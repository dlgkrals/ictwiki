package com.ict.wiki.document.dto.response;

import com.ict.wiki.document.domain.DocumentLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 문서 링크 응답 DTO
 * - 역참조 (들어오는 링크) / 나가는 링크 공용
 */
@Getter
@Builder
@AllArgsConstructor
public class DocumentLinkResponse {

    // 출발 문서
    private Long sourceDocumentId;
    private String sourceDocumentTitle;

    // 도착 문서
    private Long targetDocumentId;
    private String targetDocumentTitle;

    // 링크에 표시된 텍스트
    private String displayText;

    /**
     * Entity → Response DTO 변환
     */
    public static DocumentLinkResponse from(DocumentLink link) {
        return DocumentLinkResponse.builder()
                .sourceDocumentId(link.getSourceDocument().getId())
                .sourceDocumentTitle(link.getSourceDocument().getTitle())
                .targetDocumentId(link.getTargetDocument().getId())
                .targetDocumentTitle(link.getTargetDocument().getTitle())
                .displayText(link.getDisplayText())
                .build();
    }
}