package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.Classroom;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClassroomWithSoftwaresResponse {
    private final Long classroomId;
    private final Integer roomNumber;
    private final Integer floor;
    private final Integer grade;
    private final List<ClassroomSoftwareResponse> softwares;

    public static ClassroomWithSoftwaresResponse of(Classroom c, List<ClassroomSoftwareResponse> softwares) {
        return ClassroomWithSoftwaresResponse.builder()
                .classroomId(c.getId())
                .roomNumber(c.getRoomNumber())
                .floor(c.getFloor())
                .grade(c.getGrade())
                .softwares(softwares)
                .build();
    }
}
