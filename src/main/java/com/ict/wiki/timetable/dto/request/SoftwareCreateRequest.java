package com.ict.wiki.timetable.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class SoftwareCreateRequest {

    @NotBlank(message = "소프트웨어 명칭은 필수입니다.")
    private String name;

    private List<String> aliases;

    @NotNull(message = "기본 소프트웨어 여부는 필수입니다.")
    private Boolean isDefault;
}
