package com.ict.wiki.inquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryStatusResponse {

    /**
     * 상태 코드 (프론트에서 사용하는 값)
     * 예: "PENDING", "IN_PROGRESS"
     */
    private String code;

    /**
     * 화면에 표시될 이름
     * 예: "시작 전", "진행 중"
     */
    private String displayName;
}
