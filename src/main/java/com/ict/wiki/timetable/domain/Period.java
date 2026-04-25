package com.ict.wiki.timetable.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 교시 마스터
 * - CODE 시트 데이터를 DB로 관리
 * - 주간/야간 구분, 교시 번호 → 실제 시간 매핑
 * - 예: 주 1교시 → 09:00 ~ 09:50
 */
@Entity
@Table(name = "periods",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_period_type_number", columnNames = {"type", "period_number"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주간/야간 구분
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PeriodType type;

    /**
     * 교시 번호
     * - 주간: 1 ~ 12
     * - 야간: 1 ~ 6
     */
    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    /**
     * 수업 시작 시간
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * 수업 종료 시간
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // ========== 생성 메서드 ==========

    public static Period of(PeriodType type, int periodNumber, LocalTime startTime, LocalTime endTime) {
        Period period = new Period();
        period.type = type;
        period.periodNumber = periodNumber;
        period.startTime = startTime;
        period.endTime = endTime;
        return period;
    }
}