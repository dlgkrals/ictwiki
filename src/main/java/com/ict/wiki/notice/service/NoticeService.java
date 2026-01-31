package com.ict.wiki.notice.service;

import com.ict.wiki.notice.domain.Notice;
import com.ict.wiki.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공지사항 서비스
 * - 단순 CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // ========== 생성 ==========

    /**
     * 공지사항 생성
     */
    @Transactional
    public Notice createNotice(String title, String content) {
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .build();

        Notice saved = noticeRepository.save(notice);
        log.info("공지사항 생성 완료 - ID: {}, Title: {}", saved.getId(), title);
        return saved;
    }

    // ========== 조회 ==========

    /**
     * 공지사항 단건 조회
     */
    public Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 목록 (최신순)
     */
    public List<Notice> findAll() {
        return noticeRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 최근 N개 (홈용)
     */
    public List<Notice> findRecent(int limit) {
        return noticeRepository.findRecentNotices(limit);
    }

    // ========== 검색 ==========

    /**
     * 제목 검색
     */
    public List<Notice> searchByTitle(String keyword) {
        return noticeRepository.searchByTitle(keyword);
    }

    /**
     * 제목 + 내용 검색
     */
    public List<Notice> search(String keyword) {
        return noticeRepository.search(keyword);
    }

    // ========== 수정 ==========

    /**
     * 공지사항 수정
     */
    @Transactional
    public Notice update(Long id, String title, String content) {
        Notice notice = findById(id);
        notice.update(title, content);

        log.info("공지사항 수정 완료 - ID: {}, Title: {}", id, title);
        return notice;
    }

    // ========== 삭제 ==========

    /**
     * 공지사항 삭제 (하드 삭제)
     */
    @Transactional
    public void delete(Long id) {
        Notice notice = findById(id);
        noticeRepository.delete(notice);

        log.info("공지사항 삭제 - ID: {}, Title: {}", id, notice.getTitle());
    }
}