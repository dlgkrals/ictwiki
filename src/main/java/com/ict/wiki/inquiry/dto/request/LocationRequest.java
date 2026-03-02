package com.ict.wiki.inquiry.dto.request;

import com.ict.wiki.inquiry.domain.Building;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 위치 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class LocationRequest {

    private Building building;

    @Min(value = 1, message = "호실 번호는 1 이상이어야 합니다")
    private Integer roomNumber;

    private String roomName;
}