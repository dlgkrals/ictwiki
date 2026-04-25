package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.TimetableErrorCode;

public class TimetableException extends BaseException {

    private TimetableException(TimetableErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static TimetableException of(TimetableErrorCode errorCode, Object... args) {
        return new TimetableException(errorCode, args);
    }

    public static TimetableException of(TimetableErrorCode errorCode) {
        return new TimetableException(errorCode);
    }
}