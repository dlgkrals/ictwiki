package com.ict.wiki.timetable.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClassroomCreateRequest {

    @NotNull(message = "호실 번호는 필수입니다.")
    @Min(value = 100, message = "호실 번호는 100 이상이어야 합니다.")
    @Max(value = 999, message = "호실 번호는 999 이하여야 합니다.")
    private Integer roomNumber;

    @NotNull(message = "층은 필수입니다.")
    @Min(value = 1, message = "층은 1 이상이어야 합니다.")
    private Integer floor;

    @NotNull(message = "등급은 필수입니다.")
    @Min(value = 1, message = "등급은 1 이상이어야 합니다.")
    @Max(value = 5, message = "등급은 5 이하여야 합니다.")
    private Integer grade;
}
