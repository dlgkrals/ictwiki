package com.ict.wiki.timetable.dto.response;

import com.ict.wiki.timetable.domain.Software;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SoftwareResponse {
    private final Long id;
    private final String name;
    private final List<String> aliases;
    private final boolean isDefault;
    private final boolean versionSensitive;

    public static SoftwareResponse from(Software s, List<String> aliases) {
        return SoftwareResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .aliases(aliases)
                .isDefault(s.isDefault())
                .versionSensitive(s.isVersionSensitive())
                .build();
    }
}