package com.ict.wiki.timetable.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 소프트웨어 자동 매칭 실패 내역
 * - 학기별 마지막 실행 결과만 저장
 */
@Entity
@Table(name = "software_match_failures",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_semester_room_note",
                columnNames = {"semester", "room_number", "note"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SoftwareMatchFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String semester;

    @Column(nullable = false)
    private Integer roomNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private boolean resolved = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static SoftwareMatchFailure of(String semester, Integer roomNumber, String note) {
        SoftwareMatchFailure f = new SoftwareMatchFailure();
        f.semester = semester;
        f.roomNumber = roomNumber;
        f.note = note;
        f.resolved = false;
        f.createdAt = LocalDateTime.now();
        return f;
    }

    public void resolve() {
        this.resolved = true;
    }
}
