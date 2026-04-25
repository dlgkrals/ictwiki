package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.SoftwareMatchFailure;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareMatchFailureResponse {
    private final Long id;
    private final String semester;
    private final Integer roomNumber;
    private final String note;
    private final boolean resolved;
    private final LocalDateTime createdAt;

    public static SoftwareMatchFailureResponse from(SoftwareMatchFailure f) {
        return SoftwareMatchFailureResponse.builder()
                .id(f.getId())
                .semester(f.getSemester())
                .roomNumber(f.getRoomNumber())
                .note(f.getNote())
                .resolved(f.isResolved())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
