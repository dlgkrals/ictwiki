package com.ict.wiki.timetable.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class MakeupAvailableRequest {

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalTime endTime;

    private List<Long> softwareIds; // 필요 소프트웨어 ID 목록 (없으면 전체)
}
