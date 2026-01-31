package com.ict.wiki.inquiry.domain;

import lombok.Getter;

/**
 * 민원 유형
 * - 프론트엔드 INQUIRY_TYPES와 매칭
 */
@Getter
public enum InquiryType {
    PC("PC"),
    NETWORK("네트워크"),
    SOFTWARE("소프트웨어"),
    MULTIFUNCTION("복합기"),
    PRINTER("프린터"),
    PODIUM("전자교탁"),
    PROJECTOR("빔프로젝터"),
    PERIPHERAL("주변기기");

    private final String description;

    InquiryType(String description) {
        this.description = description;
    }

    /**
     * 한글 이름으로 Enum 찾기
     * 예: "PC" → InquiryType.PC
     */
    public static InquiryType fromDescription(String description) {
        for (InquiryType type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("잘못된 유형입니다: " + description);
    }
}