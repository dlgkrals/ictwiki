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
    private final User author;
    private final Document document;

    public DocumentCreatedEvent(Document document) {
        this.document = document;
        this.documentId = document.getId();
        this.title = document.getTitle();
        this.content = document.getContent();
        this.author = document.getAuthor();
    }
}