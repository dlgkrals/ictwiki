package com.ict.wiki.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private ErrorResponse(HttpStatus status, String code, String message) {
        this.status = status.value();
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(HttpStatus status, String code, String message) {
        return new ErrorResponse(status, code, message);
    }
}