package com.ict.wiki.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements ErrorCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQ_001", "민원을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getFullCode() {
        return code;
    }
}