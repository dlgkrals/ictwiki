package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.RagErrorCode;

public class RagException extends BaseException {

    private RagException(RagErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static RagException of(RagErrorCode errorCode, Object... args) {
        return new RagException(errorCode, args);
    }
}