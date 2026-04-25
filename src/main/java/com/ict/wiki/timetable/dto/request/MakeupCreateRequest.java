package com.ict.wiki.timetable.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class MakeupCreateRequest {

    @NotNull(message = "강의실 ID는 필수입니다.")
    private Long classroomId;

    @NotBlank(message = "학과는 필수입니다.")
    private String department;

    private String courseName;

    private String professor;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalTime endTime;

    private String softwareNote;

    private String purpose;

    private String note;
}
