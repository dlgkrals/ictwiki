package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.ClassroomSoftware;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassroomSoftwareResponse {
    private final Long id;
    private final Long softwareId;
    private final String softwareName;
    private final boolean isDefault;

    public static ClassroomSoftwareResponse from(ClassroomSoftware cs) {
        return ClassroomSoftwareResponse.builder()
                .id(cs.getId())
                .softwareId(cs.getSoftware().getId())
                .softwareName(cs.getSoftware().getName())
                .isDefault(cs.getSoftware().isDefault())
                .build();
    }
}