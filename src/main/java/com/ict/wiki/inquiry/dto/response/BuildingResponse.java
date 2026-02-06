package com.ict.wiki.inquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 건물 응답 DTO
 */
@Getter
@AllArgsConstructor
public class BuildingResponse {

    /**
     * 건물 코드 (프론트에서 사용하는 값)
     * 예: "HEUNGHAK", "HOCHEON"
     */
    private String code;

    /**
     * 화면에 표시될 이름
     * 예: "흥학관", "호천관"
     */
    private String displayName;
}