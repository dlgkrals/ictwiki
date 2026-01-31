package com.ict.wiki.notice.dto.response;

import com.ict.wiki.notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 목록용 응답 DTO (간단 버전)
 */
@Getter
@Builder
@AllArgsConstructor
public class NoticeSummaryResponse {

    private Long id;
    private String title;
    private LocalDateTime createdAt;

    /**
     * Notice 엔티티에서 Summary DTO 생성
     */
    public static NoticeSummaryResponse from(Notice notice) {
        return NoticeSummaryResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}