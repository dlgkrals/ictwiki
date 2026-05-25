package com.ict.wiki.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DailyReportResponse {

    private String date;           // "2026-05-20"
    private String userName;       // "홍길동"
    private String userRole;       // "사원"
    private String sheetName;      // "5월20일"

    // 위쪽 섹션 (완료 + 진행중)
    private List<InquiryRowData> allSection;
    private int topCount;          // max(allSection 행수, 10) - 빈 행 채우기용

    // 아래쪽 섹션 (진행중만)
    private List<InquiryRowData> inProgressSection;
    private int bottomCount;       // max(inProgressSection 행수, 3)

    @Getter
    @Builder
    public static class InquiryRowData {
        private List<LocationLine> locations;  // 위치 라인 목록 (병합 행 수 결정)
        private String receivedTime;           // "HH:mm" (접수시간)
        private String description;            // 증상
        private String solution;               // 처리내역
        private String completedTime;          // "HH:mm" (처리시간, allSection만)
        private String status;                 // "완료" / "진행중"
    }

    @Getter
    @Builder
    public static class LocationLine {
        private String building;   // 건물명
        private String room;       // 호실
    }
}