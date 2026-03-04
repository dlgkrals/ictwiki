package com.ict.wiki.inquiry.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 민원 상태
 * - 프론트엔드 INQUIRY_STATUS와 매칭
 */
@Getter
public enum InquiryStatus {
    PENDING("시작 전", true),
    IN_PROGRESS("진행 중", true),
    ON_HOLD("보류", true),
    OVERNIGHT("야간", true),
    COMPLETED("완료", false);  // 드롭다운에서 숨김

    private final String description;
    private final boolean dropdownVisible;

    InquiryStatus(String description, boolean dropdownVisible) {
        this.description = description;
        this.dropdownVisible = dropdownVisible;
    }

    public static List<InquiryStatus> getDropdownOptions() {
        return Arrays.stream(values())
                .filter(InquiryStatus::isDropdownVisible)
                .toList();
    }
}