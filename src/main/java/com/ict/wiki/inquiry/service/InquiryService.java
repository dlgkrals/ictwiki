package com.ict.wiki.inquiry.service;

import com.ict.wiki.inquiry.domain.*;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 민원 서비스
 * - 단순 CRUD (이벤트 없음)
 * - 이력 관리 없음
 * - 링크 파싱 없음
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserService userService;

    // ========== 생성 ==========

    /**
     * 민원 생성
     */
    @Transactional
    public Inquiry createInquiry(String title, InquiryType type, String description, String requester) {
        Inquiry inquiry = Inquiry.builder()
                .title(title)
                .type(type)
                .status(InquiryStatus.PENDING)
                .description(description)
                .requester(requester)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        log.info("민원 생성 완료 - ID: {}, Title: {}", saved.getId(), title);
        return saved;
    }

    // ========== 조회 ==========

    /**
     * 민원 단건 조회
     */
    public Inquiry findById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("민원을 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 목록
     */
    public List<Inquiry> findAll() {
        return inquiryRepository.findAllWithWorker();
    }

    /**
     * 최근 N개 (홈용)
     */
    public List<Inquiry> findRecent(int limit) {
        return inquiryRepository.findRecentInquiries(limit);
    }

    // ========== 필터링 ==========

    /**
     * 상태별 조회
     */
    public List<Inquiry> findByStatus(InquiryStatus status) {
        return inquiryRepository.findByStatus(status);
    }

    /**
     * 유형별 조회
     */
    public List<Inquiry> findByType(InquiryType type) {
        return inquiryRepository.findByType(type);
    }

    /**
     * 작업자별 조회
     */
    public List<Inquiry> findByWorkerId(Long workerId) {
        return inquiryRepository.findByWorkerId(workerId);
    }

    /**
     * 작업자별 + 상태별 조회
     */
    public List<Inquiry> findByWorkerIdAndStatus(Long workerId, InquiryStatus status) {
        return inquiryRepository.findByWorkerIdAndStatus(workerId, status);
    }

    /**
     * 요청자 검색
     */
    public List<Inquiry> findByRequester(String requester) {
        return inquiryRepository.findByRequester(requester);
    }

    /**
     * 미배정 민원
     */
    public List<Inquiry> findUnassigned() {
        return inquiryRepository.findUnassignedInquiries();
    }

    // ========== 검색 ==========

    /**
     * 제목 검색
     */
    public List<Inquiry> searchByTitle(String keyword) {
        return inquiryRepository.searchByTitle(keyword);
    }

    /**
     * 제목 + 설명 검색
     */
    public List<Inquiry> search(String keyword) {
        return inquiryRepository.searchByTitleOrDescription(keyword);
    }

    // ========== 수정 ==========

    /**
     * 민원 전체 수정
     */
    @Transactional
    public Inquiry update(Long id, String title, InquiryType type, InquiryStatus status,
                          Long workerId, LocalDate workDate, InquiryMethod method,
                          String description, String solution, String requester) {
        Inquiry inquiry = findById(id);

        User worker = null;
        if (workerId != null) {
            worker = userService.findById(workerId);
        }

        inquiry.update(title, type, status, worker, workDate, method, description, solution, requester);

        log.info("민원 수정 완료 - ID: {}, Title: {}", id, title);
        return inquiry;
    }

    /**
     * 상태만 변경
     */
    @Transactional
    public Inquiry updateStatus(Long id, InquiryStatus status) {
        Inquiry inquiry = findById(id);
        inquiry.updateStatus(status);

        log.info("민원 상태 변경 - ID: {}, Status: {}", id, status);
        return inquiry;
    }

    /**
     * 작업자 배정
     */
    @Transactional
    public Inquiry assignWorker(Long inquiryId, Long workerId, InquiryMethod method) {
        Inquiry inquiry = findById(inquiryId);
        User worker = userService.findById(workerId);

        inquiry.assignWorker(worker, method);

        log.info("작업자 배정 - InquiryId: {}, WorkerId: {}, Worker: {}, Method: {}",
                inquiryId, workerId, worker.getName(), method);
        return inquiry;
    }

    /**
     * 작업 완료
     */
    @Transactional
    public Inquiry complete(Long id, String solution) {
        Inquiry inquiry = findById(id);
        inquiry.complete(solution);

        log.info("민원 완료 - ID: {}, Solution: {}", id, solution);
        return inquiry;
    }

    /**
     * 보류 처리
     */
    @Transactional
    public Inquiry hold(Long id, String reason) {
        Inquiry inquiry = findById(id);
        inquiry.hold(reason);

        log.info("민원 보류 - ID: {}, Reason: {}", id, reason);
        return inquiry;
    }

    // ========== 삭제 ==========

    /**
     * 민원 삭제 (하드 삭제)
     */
    @Transactional
    public void delete(Long id) {
        Inquiry inquiry = findById(id);
        inquiryRepository.delete(inquiry);

        log.info("민원 삭제 - ID: {}, Title: {}", id, inquiry.getTitle());
    }

    // ========== 통계 ==========

    /**
     * 내가 완료한 민원 개수
     */
    public long countCompletedByWorker(Long workerId) {
        return inquiryRepository.countCompletedByWorkerId(workerId);
    }

    /**
     * 내가 진행 중인 민원 개수
     */
    public long countInProgressByWorker(Long workerId) {
        return inquiryRepository.countInProgressByWorkerId(workerId);
    }

    /**
     * 대기 중인 민원 개수
     */
    public long countPending() {
        return inquiryRepository.countPendingInquiries();
    }

    /**
     * 상태별 통계
     */
    public List<Object[]> getStatsByStatus() {
        return inquiryRepository.countByStatus();
    }

    /**
     * 유형별 통계
     */
    public List<Object[]> getStatsByType() {
        return inquiryRepository.countByType();
    }

    /**
     * 작업자별 완료 통계
     */
    public List<Object[]> getStatsCompletedByWorker() {
        return inquiryRepository.countCompletedByWorker();
    }
}