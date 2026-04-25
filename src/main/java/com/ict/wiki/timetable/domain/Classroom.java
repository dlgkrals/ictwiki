package com.ict.wiki.timetable.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 강의실
 * - 배양관 PC 실습실 34개 관리
 * - 등급(1~5)으로 PC 사양 구분
 */
@Entity
@Table(name = "classrooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 호실 번호
     * - 예: 201, 208, 410
     */
    @Column(name = "room_number", nullable = false, unique = true)
    private Integer roomNumber;

    /**
     * 층
     * - 2, 3, 4
     */
    @Column(nullable = false)
    private Integer floor;

    /**
     * PC 사양 등급
     * - 1등급(최고) ~ 5등급
     * - 등급별로 설치 가능한 소프트웨어가 다름
     */
    @Column
    private Integer grade;

    /**
     * 강의실-소프트웨어 매핑 목록
     */
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassroomSoftware> classroomSoftwares = new ArrayList<>();

    // ========== 생성 메서드 ==========

    public static Classroom of(int roomNumber, int floor, int grade) {
        Classroom classroom = new Classroom();
        classroom.roomNumber = roomNumber;
        classroom.floor = floor;
        classroom.grade = grade;
        return classroom;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 등급 변경
     */
    public void updateGrade(int grade) {
        this.grade = grade;
    }
}