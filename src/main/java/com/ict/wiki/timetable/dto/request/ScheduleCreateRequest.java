package com.ict.wiki.timetable.dto.request;

import com.ict.wiki.timetable.domain.DayOfWeek;
import com.ict.wiki.timetable.domain.PeriodType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ScheduleCreateRequest {

    @NotBlank(message = "학기는 필수입니다.")
    private String semester;           // 예: "2025-2"

    private Long classroomId;

    @NotBlank(message = "학과는 필수입니다.")
    private String department;

    private Integer grade;

    private String section;            // 예: "A", "B"

    @NotBlank(message = "교과목명은 필수입니다.")
    private String courseName;

    @NotBlank(message = "교수명은 필수입니다.")
    private String professor;

    @NotNull(message = "요일은 필수입니다.")
    private DayOfWeek dayOfWeek;       // MON, TUE, WED, THU, FRI

    @NotNull(message = "주/야 구분은 필수입니다.")
    private PeriodType periodType;     // DAY, NIGHT

    @NotNull(message = "시작 교시는 필수입니다.")
    @Min(value = 1, message = "시작 교시는 1 이상이어야 합니다.")
    private Integer periodStart;

    @NotNull(message = "종료 교시는 필수입니다.")
    @Min(value = 1, message = "종료 교시는 1 이상이어야 합니다.")
    private Integer periodEnd;

    private String softwareNote;       // 신청 소프트웨어 원문

    private Boolean hasOption = false; // 특수 옵션 여부

    private String optionNote;         // 옵션 상세

    private Integer priority;          // 배정 순위
}
