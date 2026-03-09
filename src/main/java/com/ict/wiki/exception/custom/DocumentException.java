package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.DocumentErrorCode;

public class DocumentException extends BaseException {

    private DocumentException(DocumentErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static DocumentException of(DocumentErrorCode errorCode, Object... args) {
        return new DocumentException(errorCode, args);
    }
}