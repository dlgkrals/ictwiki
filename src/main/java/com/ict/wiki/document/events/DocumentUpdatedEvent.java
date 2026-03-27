package com.ict.wiki.document.events;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.login.domain.User;
import lombok.Getter;

/**
 * 문서 수정 이벤트
 * - 문서가 수정되었을 때 발행
 */
@Getter
public class DocumentUpdatedEvent {

    private final Long documentId;
    private final String oldTitle;
    private final String oldContent;
    private final String newTitle;
    private final String newContent;
    private final Integer oldVersion;
    private final Long editorId;
    private final String editorName;
    private final String editReason;

    public DocumentUpdatedEvent(Long documentId,
                                String oldTitle, String oldContent,
                                String newTitle, String newContent,
                                Integer oldVersion,
                                Long editorId, String editorName,
                                String editReason) {
        this.documentId = documentId;
        this.oldTitle = oldTitle;
        this.oldContent = oldContent;
        this.newTitle = newTitle;
        this.newContent = newContent;
        this.oldVersion = oldVersion;
        this.editorId = editorId;
        this.editorName = editorName;
        this.editReason = editReason;
    }
}