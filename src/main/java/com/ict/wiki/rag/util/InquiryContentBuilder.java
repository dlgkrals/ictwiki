package com.ict.wiki.rag.util;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryLocation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

/**
 * RAG 임베딩용 민원 텍스트 포맷 유틸
 * - "유형: X | 위치: Y | 문제: Z | 해결: W" 형식으로 변환
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InquiryContentBuilder {

    public static String build(Inquiry inquiry) {
        String location = inquiry.getLocations().isEmpty()
                ? "위치 미상"
                : inquiry.getLocations().stream()
                .map(InquiryLocation::getFormatted)
                .collect(Collectors.joining(", "));

        return String.format("유형: %s | 위치: %s | 문제: %s | 해결: %s",
                inquiry.getType().getDescription(),
                location,
                inquiry.getDescription(),
                inquiry.getSolution());
    }
}