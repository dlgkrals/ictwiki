package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.NoticeErrorCode;

public class NoticeException extends BaseException {

    private NoticeException(NoticeErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static NoticeException of(NoticeErrorCode errorCode, Object... args) {
        return new NoticeException(errorCode, args);
    }

}