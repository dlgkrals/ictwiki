package com.ict.wiki.timetable.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ScheduleExportResponse {

    private String semester;
    private List<FloorData> floors;

    @Getter
    @Builder
    public static class FloorData {
        private String name;           // "2F공용PC실습실"
        private List<Integer> rooms;   // [201, 202, ...]
        private List<DayData> days;
    }

    @Getter
    @Builder
    public static class DayData {
        private String day;            // "MON"
        private String label;          // "월"
        private List<PeriodRow> dayPeriods;    // 주간 교시별
        private List<NightGroup> nightGroups;  // 야간 그룹별
    }

    @Getter
    @Builder
    public static class PeriodRow {
        private String periodLabel;            // "1교시(09:00～09:50)"
        private Map<Integer, CellData> cells;  // roomNumber → 수업정보 (null이면 빈 셀)
    }

    @Getter
    @Builder
    public static class NightGroup {
        private List<String> periodLabels;     // ["1교시(17:30～18:15)", ...]
        private Map<Integer, CellData> cells;
    }

    @Getter
    @Builder
    public static class CellData {
        private String courseName;
        private String professor;
        private String grade;
        private String section;
        private int rowSpan;
    }
}