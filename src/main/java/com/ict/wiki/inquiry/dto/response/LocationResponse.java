package com.ict.wiki.inquiry.dto.response;

import com.ict.wiki.inquiry.domain.InquiryLocation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationResponse {

    private String buildingCode;
    private String buildingName;
    private Integer roomNumber;
    private String roomName;
    private String formatted;

    public static LocationResponse from(InquiryLocation location) {
        return LocationResponse.builder()
                .buildingCode(location.getBuilding().name())
                .buildingName(location.getBuilding().getDisplayName())
                .roomNumber(location.getRoomNumber())
                .roomName(location.getRoomName())
                .formatted(location.getFormatted())
                .build();
    }
}