package com.ict.wiki.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

    EMPTY_FILE(HttpStatus.BAD_REQUEST, "FILE_001", "파일이 비어있습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "허용되지 않는 파일 형식입니다 (jpg/png/gif/webp만 가능)"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_003", "파일 크기는 10MB를 초과할 수 없습니다"),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_004", "파일 저장에 실패했습니다"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_005", "파일을 찾을 수 없습니다"),
    INVALID_FILENAME(HttpStatus.BAD_REQUEST, "FILE_006", "잘못된 파일명입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getFullCode() {
        return code;
    }
}