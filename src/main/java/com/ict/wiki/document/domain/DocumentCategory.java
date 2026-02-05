package com.ict.wiki.document.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 문서 카테고리
 */
@Getter
@RequiredArgsConstructor
public enum DocumentCategory {
    WORK("업무", "업무 관련 문서"),
    INQUIRY("민원", "민원 처리 문서"),
    SOLUTION("해결 방법", "문제 해결 방법"),
    TASK("작업", "작업 지시 및 기록"),
    STORAGE("창고", "장비 및 물품 보관"),
    EQUIPMENT("장비", "전자교탁, 빔프로젝터 등 장비");

    private final String displayName;
    private final String description;

    /**
     * 코드로부터 카테고리 찾기
     */
    public static DocumentCategory fromCode(String code) {
        return Arrays.stream(values())
                .filter(category -> category.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리 코드입니다: " + code));
    }
}