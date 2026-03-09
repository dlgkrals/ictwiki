package com.ict.wiki.exception.custom;

import com.ict.wiki.exception.BaseException;
import com.ict.wiki.exception.code.InquiryErrorCode;

public class InquiryException extends BaseException {

    private InquiryException(InquiryErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static InquiryException of(InquiryErrorCode errorCode, Object... args) {
        return new InquiryException(errorCode, args);
    }
}