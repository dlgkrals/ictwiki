package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.FileErrorCode;

public class FileException extends BaseException {

    private FileException(FileErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static FileException of(FileErrorCode errorCode, Object... args) {
        return new FileException(errorCode, args);
    }
}