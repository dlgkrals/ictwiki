package com.ict.wiki.inquiry.controller;

import com.ict.wiki.inquiry.domain.Building;
import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.domain.InquiryType;
import com.ict.wiki.inquiry.dto.request.*;
import com.ict.wiki.inquiry.dto.response.BuildingResponse;
import com.ict.wiki.inquiry.dto.response.InquiryResponse;
import com.ict.wiki.inquiry.dto.response.InquirySummaryResponse;
import com.ict.wiki.inquiry.service.InquiryService;
import com.ict.wiki.security.auth.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 민원 API
 */
@Slf4j
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // ========== 생성 ==========

    /**
     * 건물 목록 조회
     * GET /api/inquiries/buildings
     */
    @GetMapping("/buildings")
    public ResponseEntity<List<BuildingResponse>> getAllBuildings() {
        List<BuildingResponse> buildings = Arrays.stream(Building.values())
                .map(building -> new BuildingResponse(
                        building.name(),            // code: "HEUNGHAK"
                        building.getDisplayName()   // displayName: "흥학관"
                ))
                .toList();

        return ResponseEntity.ok(buildings);
    }

    /**
     * 민원 생성
     * POST /api/inquiries
     */
    @PostMapping
    public ResponseEntity<InquiryResponse> createInquiry(
            @Valid @RequestBody InquirySaveRequest request,  // ← DTO 변경
            HttpSession session) {

        Inquiry inquiry = inquiryService.createInquiry(request);  // ← 파라미터 간소화

        return ResponseEntity.ok(InquiryResponse.from(inquiry));
    }

    // ========== 조회 ==========

    /**
     * 민원 단건 조회
     * GET /api/inquiries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiry(@PathVariable Long id) {
        Inquiry inquiry = inquiryService.findById(id);
        return ResponseEntity.ok(InquiryResponse.from(inquiry));
    }

    /**
     * 전체 목록 조회
     * GET /api/inquiries
     */
    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getAllInquiries() {
        List<Inquiry> inquiries = inquiryService.findAll();

        List<InquiryResponse> response = inquiries.stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 최근 N개 조회 (홈용)
     * GET /api/inquiries/recent?limit=5
     */
    @GetMapping("/recent")
    public ResponseEntity<List<InquirySummaryResponse>> getRecentInquiries(
            @RequestParam(defaultValue = "10") int limit) {

        List<Inquiry> inquiries = inquiryService.findRecent(limit);

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== 필터링 ==========

    /**
     * 상태별 조회
     * GET /api/inquiries/by-status?status=PENDING
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<InquirySummaryResponse>> getInquiriesByStatus(
            @RequestParam InquiryStatus status) {

        List<Inquiry> inquiries = inquiryService.findByStatus(status);

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 유형별 조회
     * GET /api/inquiries/by-type?type=PC
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<InquirySummaryResponse>> getInquiriesByType(
            @RequestParam InquiryType type) {

        List<Inquiry> inquiries = inquiryService.findByType(type);

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 내가 담당한 민원
     * GET /api/inquiries/my-assigned
     */
    @GetMapping("/my-assigned")
    public ResponseEntity<List<InquirySummaryResponse>> getMyAssignedInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Inquiry> inquiries = inquiryService.findByWorkerId(userDetails.getId());

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 내가 담당한 민원 (상태별)
     * GET /api/inquiries/my-assigned/by-status?status=IN_PROGRESS
     */
    @GetMapping("/my-assigned/by-status")
    public ResponseEntity<List<InquirySummaryResponse>> getMyAssignedInquiriesByStatus(
            @RequestParam InquiryStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Inquiry> inquiries = inquiryService.findByWorkerIdAndStatus(userDetails.getId(), status);

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 미배정 민원
     * GET /api/inquiries/unassigned
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<InquirySummaryResponse>> getUnassignedInquiries() {
        List<Inquiry> inquiries = inquiryService.findUnassigned();

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== 검색 ==========

    /**
     * 제목 + 설명 검색
     * GET /api/inquiries/search?keyword=PC
     */
    @GetMapping("/search")
    public ResponseEntity<List<InquirySummaryResponse>> searchInquiries(
            @RequestParam String keyword) {

        List<Inquiry> inquiries = inquiryService.search(keyword);

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 요청자 검색
     * GET /api/inquiries/by-requester?requester=간호학과
     */
    @GetMapping("/by-requester")
    public ResponseEntity<List<InquirySummaryResponse>> getInquiriesByRequester(
            @RequestParam String requester) {

        List<Inquiry> inquiries = inquiryService.findByRequester(requester);

        List<InquirySummaryResponse> response = inquiries.stream()
                .map(InquirySummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== 수정 ==========

    /**
     * 민원 전체 수정
     * PUT /api/inquiries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<InquiryResponse> updateInquiry(
            @PathVariable Long id,
            @Valid @RequestBody InquirySaveRequest request) {  // ← DTO 변경

        Inquiry inquiry = inquiryService.update(id, request);  // ← 파라미터 간소화

        return ResponseEntity.ok(InquiryResponse.from(inquiry));
    }

    // ========== 삭제 ==========

    /**
     * 민원 삭제
     * DELETE /api/inquiries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteInquiry(@PathVariable Long id) {
        inquiryService.delete(id);
        return ResponseEntity.ok(Map.of("message", "민원이 삭제되었습니다"));
    }

    // ========== 통계 ==========

    /**
     * 내 통계
     * GET /api/inquiries/my-stats
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        long completedCount = inquiryService.countCompletedByWorker(userDetails.getId());
        long inProgressCount = inquiryService.countInProgressByWorker(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "name", userDetails.getName(),
                "completedCount", completedCount,
                "inProgressCount", inProgressCount
        ));
    }

    /**
     * 대기 중인 민원 개수
     * GET /api/inquiries/pending-count
     */
    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        long count = inquiryService.countPending();
        return ResponseEntity.ok(Map.of("count", count));
    }
}