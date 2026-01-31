package com.ict.wiki.inquiry.domain;

import lombok.Getter;

/**
 * 민원 처리 방식
 * - 프론트엔드 INQUIRY_METHOD와 매칭
 */
@Getter
public enum InquiryMethod {
    REMOTE("원격"),
    ON_SITE("방문");

    private final String description;

    InquiryMethod(String description) {
        this.description = description;
    }

    /**
     * 한글 이름으로 Enum 찾기
     * 예: "원격" → InquiryMethod.REMOTE
     */
    public static InquiryMethod fromDescription(String description) {
        for (InquiryMethod method : values()) {
            if (method.description.equals(description)) {
                return method;
            }
        }
        throw new IllegalArgumentException("잘못된 처리 방식입니다: " + description);
    }
}