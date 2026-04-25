package com.ict.wiki.timetable.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClassroomAssignRequest {

    @NotNull(message = "강의실 ID는 필수입니다.")
    private Long classroomId;
}