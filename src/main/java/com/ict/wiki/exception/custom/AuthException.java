package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.AuthErrorCode;

public class AuthException extends BaseException {

    private AuthException(AuthErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static AuthException of(AuthErrorCode errorCode, Object... args) {
        return new AuthException(errorCode, args);
    }
}