package com.ict.wiki.document.events;

import com.ict.wiki.login.domain.User;
import lombok.Getter;

/**
 * 문서 삭제 이벤트
 * - 문서가 삭제되었을 때 발행
 */
@Getter
public class DocumentDeletedEvent {

    private final Long documentId;
    private final String title;
    private final boolean permanent;

    public DocumentDeletedEvent(Long documentId, String title, boolean permanent) {
        this.documentId = documentId;
        this.title = title;
        this.permanent = permanent;
    }
}