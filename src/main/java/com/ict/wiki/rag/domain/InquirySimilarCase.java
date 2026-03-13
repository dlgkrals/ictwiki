package com.ict.wiki.rag.domain;

import com.ict.wiki.BaseEntity;
import com.ict.wiki.inquiry.domain.Inquiry;
import jakarta.persistence.*;
import lombok.*;

/**
 * 민원별 유사 사례 분석 결과
 * - 민원 등록 시 비동기로 생성
 * - summary: LLM이 한 번만 생성한 요약 텍스트
 * - relatedInquiryIds: 유사 민원 ID 목록 (콤마 구분)
 *   → 조회 시 원본 데이터 JOIN하여 최신 상태 반영
 */
@Entity
@Table(name = "inquiry_similar_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InquirySimilarCase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대상 민원 (관계의 주인 - FK 보유)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, unique = true)
    private Inquiry inquiry;

    /**
     * LLM 요약 결과 (딱 한 번만 생성)
     * 예: "• 드라이버 재설치\n• 케이블 재연결"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    /**
     * 유사 민원 ID 목록 (콤마 구분)
     * 예: "192,437,114,451,134"
     * - 원본 데이터 중복 저장 방지
     * - 조회 시 inquiryRepository.findAllById()로 최신 데이터 반영
     */
    @Column(nullable = false, length = 200)
    private String relatedInquiryIds;

    /**
     * 검색된 유사 사례 수
     */
    @Column(nullable = false)
    private int referenceCount;

    // ===== 팩토리 메서드 =====

    public static InquirySimilarCase of(Inquiry inquiry, String summary,
                                        String relatedInquiryIds, int referenceCount) {
        return InquirySimilarCase.builder()
                .inquiry(inquiry)
                .summary(summary)
                .relatedInquiryIds(relatedInquiryIds)
                .referenceCount(referenceCount)
                .build();
    }
}