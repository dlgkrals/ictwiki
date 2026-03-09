package com.ict.wiki.exception;

import com.ict.wiki.exception.code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String message;

    protected BaseException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
        this.message = message;
    }

    protected BaseException(ErrorCode errorCode) {
        this(
                errorCode.getStatus(),
                errorCode.getFullCode(),
                errorCode.getMessage()
        );
    }

    protected BaseException(ErrorCode errorCode, Object... args) {
        this(
                errorCode.getStatus(),
                errorCode.getFullCode(),
                errorCode.format(args)  // 더 자연스럽게 읽힘
        );
    }

}