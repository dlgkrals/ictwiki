package com.ict.wiki.inquiry.dto.response;

import com.ict.wiki.inquiry.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 민원 목록용 응답 DTO (간단 버전)
 */
@Getter
@Builder
@AllArgsConstructor
public class InquirySummaryResponse {

    private Long id;
    private String title;
    private InquiryType type;
    private InquiryStatus status;

    // 작업자 정보
    private Long workerId;
    private String workerName;
    private Long subWorkerId;
    private String subWorkerName;
    private InquiryMethod method;

    // 요청자
    private String requester;

    // 건물 및 호실 정보
    private String buildingCode;
    private String buildingName;
    private String formattedRoom;

    // 시간 정보
    private LocalDateTime createdAt;

    /**
     * Inquiry 엔티티에서 Summary DTO 생성
     */
    public static InquirySummaryResponse from(Inquiry inquiry) {
        return InquirySummaryResponse.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .type(inquiry.getType())
                .status(inquiry.getStatus())
                .workerId(inquiry.getWorker() != null ? inquiry.getWorker().getId() : null)
                .workerName(inquiry.getWorker() != null ? inquiry.getWorker().getName() : null)
                .subWorkerId(inquiry.getSubWorker() != null ? inquiry.getSubWorker().getId() : null)
                .subWorkerName(inquiry.getSubWorker() != null ? inquiry.getSubWorker().getName() : null)
                .method(inquiry.getMethod())
                .requester(inquiry.getRequester())
                .buildingCode(inquiry.getBuilding() != null ? inquiry.getBuilding().name() : null)
                .buildingName(inquiry.getBuilding() != null ? inquiry.getBuilding().getDisplayName() : null)
                .formattedRoom(inquiry.getRoomNumber() != null ? inquiry.getRoomNumber() + " 호" : null)
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}