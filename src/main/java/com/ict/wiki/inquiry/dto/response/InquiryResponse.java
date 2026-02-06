package com.ict.wiki.inquiry.dto.response;

import com.ict.wiki.inquiry.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 민원 상세 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InquiryResponse {

    private Long id;
    private String title;
    private InquiryType type;
    private InquiryStatus status;

    // 작업자 정보
    private Long workerId;
    private String workerName;
    private LocalDate workDate;
    private InquiryMethod method;

    // 민원 내용
    private String description;
    private String solution;
    private String requester;

    // 건물 및 호실 정보
    private String buildingCode;
    private String buildingName;
    private String formattedRoom;  // "502 호" 형식

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String createdBy;
    private String modifiedBy;

    /**
     * Inquiry 엔티티에서 Response DTO 생성
     */
    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .type(inquiry.getType())
                .status(inquiry.getStatus())
                .workerId(inquiry.getWorker() != null ? inquiry.getWorker().getId() : null)
                .workerName(inquiry.getWorker() != null ? inquiry.getWorker().getName() : null)
                .workDate(inquiry.getWorkDate())
                .method(inquiry.getMethod())
                .description(inquiry.getDescription())
                .solution(inquiry.getSolution())
                .requester(inquiry.getRequester())
                .buildingCode(inquiry.getBuilding() != null ? inquiry.getBuilding().name() : null)
                .buildingName(inquiry.getBuilding() != null ? inquiry.getBuilding().getDisplayName() : null)
                .formattedRoom(inquiry.getRoomNumber() != null ? inquiry.getRoomNumber() + " 호" : null)
                .createdAt(inquiry.getCreatedAt())
                .modifiedAt(inquiry.getModifiedAt())
                .createdBy(inquiry.getCreatedBy())
                .modifiedBy(inquiry.getModifiedBy())
                .build();
    }
}