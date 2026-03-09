package com.ict.wiki.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 회원가입
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "AUTH_001", "이미 존재하는 이메일입니다"),

    // 로그인
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_002", "사용자를 찾을 수 없습니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_003", "이메일 또는 비밀번호가 일치하지 않습니다. (남은 시도: %d회)"),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH_004", "로그인 시도 횟수를 초과했습니다. %d분 %d초 후 다시 시도해주세요."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_005", "이메일 인증이 완료되지 않은 계정입니다"),
    NOT_APPROVED(HttpStatus.FORBIDDEN, "AUTH_006", "관리자 승인 대기 중입니다"),

    // 비밀번호 변경
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_007", "현재 비밀번호가 일치하지 않습니다"),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "AUTH_008", "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다"),

    // 이메일 인증
    EMAIL_SEND_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "AUTH_009", "이메일은 60초에 한 번만 재전송할 수 있습니다. %d초 후 다시 시도해주세요."),
    EMAIL_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_010", "이메일 재전송은 최대 3회까지 가능합니다"),
    EMAIL_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_011", "유효하지 않거나 만료된 인증 토큰입니다. 인증 메일을 재발송해주세요."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_012", "이미 인증된 계정입니다"),
    ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "AUTH_013", "이미 승인된 회원입니다"),
    CANNOT_ACTIVATE_NOT_APPROVED(HttpStatus.BAD_REQUEST, "AUTH_014", "승인되지 않은 회원은 활성화할 수 없습니다"),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_015", "유효하지 않거나 만료된 재설정 토큰입니다. 재설정 메일을 다시 요청해주세요"),
    PASSWORD_RESET_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "AUTH_016", "비밀번호 재설정 메일은 5분에 한 번만 요청할 수 있습니다. %d분 %d초 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getFullCode() {
        return code;
    }
}