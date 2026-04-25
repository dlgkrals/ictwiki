package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.Makeup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class MakeupResponse {
    private final Long id;
    private final Integer roomNumber;
    private final String department;
    private final String courseName;
    private final String professor;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String softwareNote;
    private final String purpose;
    private final String note;

    public static MakeupResponse from(Makeup m) {
        return MakeupResponse.builder()
                .id(m.getId())
                .roomNumber(m.getClassroom().getRoomNumber())
                .department(m.getDepartment())
                .courseName(m.getCourseName())
                .professor(m.getProfessor())
                .date(m.getDate())
                .startTime(m.getStartTime())
                .endTime(m.getEndTime())
                .softwareNote(m.getSoftwareNote())
                .purpose(m.getPurpose())
                .note(m.getNote())
                .build();
    }
}