package com.ict.wiki.inquiry.domain;

import lombok.Getter;

/**
 * 민원 상태
 * - 프론트엔드 INQUIRY_STATUS와 매칭
 */
@Getter
public enum InquiryStatus {
    PENDING("시작 전"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    ON_HOLD("보류");

    private final String description;

    InquiryStatus(String description) {
        this.description = description;
    }

    /**
     * 한글 이름으로 Enum 찾기
     * 예: "시작 전" → InquiryStatus.PENDING
     */
    public static InquiryStatus fromDescription(String description) {
        for (InquiryStatus status : values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("잘못된 상태입니다: " + description);
    }
}