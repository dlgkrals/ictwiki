package com.ict.wiki.inquiry.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 건물 Enum
 */
@Getter
@RequiredArgsConstructor
public enum Building {
    HEUNGHAK("흥학관"),
    HOCHEON("호천관"),
    SEJONG("세종관"),
    SEOIL("서일관"),
    JIDEOK("지덕관"),
    NURI("누리관"),
    LIBRARY("도서관"),
    BAEYANG("배양관"),
    CLUB("동아리관"),
    CAFETERIA("학생식당"),
    FRONT_GATE("정문"),
    BACK_GATE("후문"),
    SANGHAK("산학관");

    private final String displayName;

    /**
     * 코드로부터 건물 찾기
     */
    public static Building fromCode(String code) {
        return Arrays.stream(values())
                .filter(building -> building.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 건물 코드입니다: " + code));
    }
}