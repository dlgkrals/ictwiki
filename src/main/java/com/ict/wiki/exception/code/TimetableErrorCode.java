package com.ict.wiki.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TimetableErrorCode implements ErrorCode {

    // 강의실
    CLASSROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_001", "강의실을 찾을 수 없습니다."),
    CLASSROOM_DUPLICATE(HttpStatus.CONFLICT, "TT_002", "이미 존재하는 호실입니다."),

    // 소프트웨어
    SOFTWARE_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_010", "소프트웨어를 찾을 수 없습니다."),
    SOFTWARE_DUPLICATE(HttpStatus.CONFLICT, "TT_011", "이미 존재하는 소프트웨어입니다."),

    // 강의실-소프트웨어
    CLASSROOM_SOFTWARE_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_020", "해당 소프트웨어가 이 강의실에 등록되어 있지 않습니다."),
    CLASSROOM_SOFTWARE_DUPLICATE(HttpStatus.CONFLICT, "TT_021", "이미 등록된 소프트웨어입니다."),

    // 시간표
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_030", "시간표를 찾을 수 없습니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "TT_031", "시간 충돌: %s"),
    PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_032", "%s 교시 정보를 찾을 수 없습니다."),

    // 보강
    MAKEUP_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_040", "보강을 찾을 수 없습니다."),
    MAKEUP_SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "TT_041", "해당 시간에 정규수업이 있어 배정할 수 없습니다."),
    MAKEUP_CONFLICT(HttpStatus.CONFLICT, "TT_042", "해당 시간에 이미 보강이 배정되어 있습니다."),
    MAKEUP_WEEKEND(HttpStatus.BAD_REQUEST, "TT_043", "주말에는 보강을 배정할 수 없습니다."),

    // 엑셀 업로드
    EXCEL_EMPTY(HttpStatus.BAD_REQUEST, "TT_050", "파일이 비어있습니다."),
    EXCEL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "TT_051", ".xlsx 파일만 업로드 가능합니다."),
    EXCEL_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TT_052", "엑셀 파일 파싱에 실패했습니다."),
    EXCEL_SHEET_NOT_FOUND(HttpStatus.BAD_REQUEST, "TT_053", "'강의실 배정' 시트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getFullCode() {
        return code;
    }
}