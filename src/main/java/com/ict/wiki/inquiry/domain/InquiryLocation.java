package com.ict.wiki.inquiry.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 민원 위치 엔티티
 * - 민원 하나에 여러 장소가 연결될 수 있음
 * - Inquiry에 종속적인 엔티티 (Inquiry 없이 단독 존재 불가)
 */
@Entity
@Table(name = "inquiry_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 민원
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    /**
     * 건물
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Building building;

    /**
     * 호실 번호 (일반 호실)
     * 예: 502
     */
    @Column
    private Integer roomNumber;

    /**
     * 호실 이름 (특수 공간)
     * 예: "소강당", "가람실", "예다움홀"
     */
    @Column(length = 100)
    private String roomName;

    // ========== 생성 메서드 ==========

    public static InquiryLocation of(Inquiry inquiry, Building building, Integer roomNumber, String roomName) {
        InquiryLocation location = new InquiryLocation();
        location.inquiry = inquiry;
        location.building = building;
        location.roomNumber = roomNumber;
        location.roomName = (roomName != null && !roomName.isBlank()) ? roomName : null;
        return location;
    }

    // ========== 조회 편의 메서드 ==========

    /**
     * 표시용 문자열 반환
     * - 호천관 705호 소강당
     * - 호천관 소강당
     * - 흥학관 502호
     * - 흥학관
     */
    public String getFormatted() {
        String base = building.getDisplayName();

        if (roomNumber != null && roomName != null) {
            return base + " " + roomNumber + "호 " + roomName;
        }
        if (roomNumber != null) {
            return base + " " + roomNumber + "호";
        }
        if (roomName != null) {
            return base + " " + roomName;
        }
        return base;
    }
}