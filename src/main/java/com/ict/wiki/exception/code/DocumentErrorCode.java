package com.ict.wiki.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DocumentErrorCode implements ErrorCode {

    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOC_001", "문서를 찾을 수 없습니다"),
    DOCUMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "DOC_002", "이미 삭제된 문서입니다"),
    DOCUMENT_CANNOT_UPDATE_DELETED(HttpStatus.BAD_REQUEST, "DOC_003", "삭제된 문서는 수정할 수 없습니다"),
    DOCUMENT_TITLE_DUPLICATE(HttpStatus.CONFLICT, "DOC_004", "이미 존재하는 제목입니다"),
    DOCUMENT_NOT_DELETED(HttpStatus.BAD_REQUEST, "DOC_005", "삭제되지 않은 문서입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getFullCode() {
        return code;
    }
}