package com.ict.wiki.inquiry.domain;

import com.ict.wiki.BaseEntity;
import com.ict.wiki.login.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 민원/작업 요청 엔티티
 */
@Entity
@Table(name = "inquiries",
        indexes = {
                @Index(name = "idx_worker_id", columnList = "worker_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_type", columnList = "type")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 작업 이름 (제목)
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 유형 (PC, 네트워크, 소프트웨어 등)
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private InquiryType type;

    /**
     * 상태 (시작 전, 진행 중, 완료, 보류)
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.PENDING;

    /**
     * 작업자 (User FK)
     * - 배정 전에는 null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private User worker;

    /**
     * 보조 작업자 (User FK)
     * - 배정 전에는 null
     * 업무 보고 엑셀에 같이 한 작업도 포함되게 하기 위함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_worker_id")
    private User subWorker;

    /**
     * 완료 날짜
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * 처리 방식 (원격/방문)
     */
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private InquiryMethod method;

    /**
     * 문제 설명 (필수)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * 해결 방법 (완료 시 작성)
     */
    @Column(columnDefinition = "TEXT")
    private String solution;

    /**
     * 요청자 (외부인, String)
     * 예: "간호학과 조교", "경영학과 교수"
     */
    @Column(nullable = false, length = 100)
    private String requester;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InquiryLocation> locations = new ArrayList<>();

    // ========== 비즈니스 메서드 ==========

    /**
     * 민원 전체 수정
     */
    public void update(String title, InquiryType type, InquiryStatus status,
                       User worker, InquiryMethod method,
                       String description, String solution, String requester) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.worker = worker;
        this.method = method;
        this.description = description;
        this.solution = solution;
        this.requester = requester;
    }

    public void updateLocations(List<InquiryLocation> newLocations) {
        this.locations.clear();
        this.locations.addAll(newLocations);
    }

    /**
     * 작업자 배정
     * - 상태가 '시작 전'이면 자동으로 '진행 중'으로 변경
     */
    public void assignWorker(User worker, InquiryMethod method) {
        this.worker = worker;
        this.method = method;
        if (this.status == InquiryStatus.PENDING) {
            this.status = InquiryStatus.IN_PROGRESS;
        }
    }

    /**
     * 보조 작업자 배정
     */
    public void assignSubWorker(User subWorker) {
        this.subWorker = subWorker;
    }

    /**
     * 보조 작업자 해제
     */
    public void removeSubWorker() {
        this.subWorker = null;
    }

    /**
     * 보류 처리
     */
    public void hold(String reason) {
        this.status = InquiryStatus.ON_HOLD;
        // 보류 사유를 solution에 임시 저장하거나 별도 필드 추가 가능
        if (this.solution == null || this.solution.isEmpty()) {
            this.solution = "[보류] " + reason;
        }
    }

    /**
     * 완료 처리
     */
    public void complete() {
        this.status = InquiryStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}