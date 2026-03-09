package com.ict.wiki.exception.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getStatus();
    String getCode();
    String getMessage();

    default String getFullCode() {
        return getCode();
    }

    default String format(Object... args) {
        return String.format(getMessage(), args);
    }
}