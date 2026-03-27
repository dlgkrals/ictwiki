package com.ict.wiki.document.events;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.login.domain.User;
import lombok.Getter;

/**
 * 문서 생성 이벤트
 * - 문서가 생성되었을 때 발행
 */
@Getter
public class DocumentCreatedEvent {

    private final Long documentId;
    private final String title;
    private final String content;
    private final Long authorId;
    private final String authorName;

    public DocumentCreatedEvent(Long documentId, String title, String content,
                                Long authorId, String authorName) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
    }
}