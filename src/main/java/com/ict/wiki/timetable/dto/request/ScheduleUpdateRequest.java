package com.ict.wiki.timetable.dto.request;

import com.ict.wiki.timetable.domain.DayOfWeek;
import com.ict.wiki.timetable.domain.PeriodType;
import lombok.Getter;

@Getter
public class ScheduleUpdateRequest {

    // 수업 정보
    private String department;
    private Integer grade;
    private String section;
    private String courseName;
    private String professor;
    private String softwareNote;
    private Boolean hasOption;
    private String optionNote;
    private Integer priority;

    //호실 있음 -> 없음으로 변경
    private Boolean clearClassroom;

    // 강의실/요일/교시 (변경 시 충돌 체크)
    private Long classroomId;
    private DayOfWeek dayOfWeek;
    private PeriodType periodType;
    private Integer periodStart;
    private Integer periodEnd;
}