package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.Classroom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassroomResponse {
    private final Long id;
    private final Integer roomNumber;
    private final Integer floor;
    private final Integer grade;

    public static ClassroomResponse from(Classroom c) {
        return ClassroomResponse.builder()
                .id(c.getId())
                .roomNumber(c.getRoomNumber())
                .floor(c.getFloor())
                .grade(c.getGrade())
                .build();
    }
}