package com.ict.wiki.timetable.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의실-소프트웨어 매핑 (중간 엔티티)
 * - Classroom과 Software의 다대다(M:N) 관계를 명시적으로 관리
 * - 조교/관리자가 학기마다 직접 수정
 * - 실제 설치 목록 기준 → 충돌 체크 시 이 테이블만 참조
 */
@Entity
@Table(name = "classroom_softwares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_classroom_software",
                        columnNames = {"classroom_id", "software_id"}
                )
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassroomSoftware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 강의실
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /**
     * 소프트웨어
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    // ========== 생성 메서드 ==========

    public static ClassroomSoftware of(Classroom classroom, Software software) {
        ClassroomSoftware cs = new ClassroomSoftware();
        cs.classroom = classroom;
        cs.software = software;
        return cs;
    }
}