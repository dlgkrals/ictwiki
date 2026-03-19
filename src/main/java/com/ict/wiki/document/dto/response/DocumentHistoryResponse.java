package com.ict.wiki.document.dto.response;

import com.ict.wiki.document.domain.DocumentHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문서 수정 이력 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class DocumentHistoryResponse {

    private Long id;
    private Long documentId;
    private Integer version;
    private String title;
    private String content;
    private String editReason;

    // 수정자 정보
    private Long editorId;
    private String editorName;
    private String editorEmail;

    // 수정 시간
    private LocalDateTime editedAt;

    /**
     * Entity → Response DTO 변환
     */
    public static DocumentHistoryResponse from(DocumentHistory history) {
        return DocumentHistoryResponse.builder()
                .id(history.getId())
                .documentId(history.getDocument().getId())
                .version(history.getVersion())
                .title(history.getTitle())
                .content(history.getContent())
                .editReason(history.getEditReason())
                .editorId(history.getEditor().getId())
                .editorName(history.getEditor().getName())
                .editorEmail(history.getEditor().getEmail())
                .editedAt(history.getEditedAt())
                .build();
    }
}