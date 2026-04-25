package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class ScheduleResponse {
    private final Long id;
    private final String semester;
    private final Integer roomNumber;
    private final String department;
    private final Integer grade;
    private final String section;
    private final String courseName;
    private final String professor;
    private final String dayOfWeek;
    private final String periodType;
    private final Integer periodStart;
    private final Integer periodEnd;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String softwareNote;
    private final boolean hasOption;
    private final String optionNote;
    private final Integer priority;

    public static ScheduleResponse from(Schedule s) {
        return ScheduleResponse.builder()
                .id(s.getId())
                .semester(s.getSemester())
                .roomNumber(s.getClassroom() != null ? s.getClassroom().getRoomNumber() : null)
                .department(s.getDepartment())
                .grade(s.getGrade())
                .section(s.getSection())
                .courseName(s.getCourseName())
                .professor(s.getProfessor())
                .dayOfWeek(s.getDayOfWeek().getLabel())
                .periodType(s.getPeriodType().getLabel())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .softwareNote(s.getSoftwareNote())
                .hasOption(s.isHasOption())
                .optionNote(s.getOptionNote())
                .priority(s.getPriority())
                .build();
    }
}