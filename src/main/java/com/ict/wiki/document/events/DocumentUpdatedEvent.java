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
    private final User editor;
    private final String editReason;
    private final Integer oldVersion;
    private final Document document;

    public DocumentUpdatedEvent(Document document,
                                String oldTitle,
                                String oldContent,
                                Integer oldVersion,
                                User editor,
                                String editReason) {
        this.document = document;
        this.documentId = document.getId();
        this.oldTitle = oldTitle;
        this.oldContent = oldContent;
        this.newTitle = document.getTitle();
        this.newContent = document.getContent();
        this.oldVersion = oldVersion;
        this.editor = editor;
        this.editReason = editReason;
    }
}