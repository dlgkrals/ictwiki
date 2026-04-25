package com.ict.wiki.timetable.domain;

import com.ict.wiki.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 보강
 * - 특정 날짜에 1회성으로 진행되는 보강 수업
 * - 조교가 메일로 받은 요청을 바탕으로 등록
 * - 충돌 체크: 같은 강의실의 정규수업(Schedule) + 다른 보강(Makeup)과 시간 겹침 확인
 */
@Entity
@Table(name = "makeups",
        indexes = {
                @Index(name = "idx_makeup_classroom_date", columnList = "classroom_id, date")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Makeup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 배정 강의실
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /**
     * 신청 학과
     */
    @Column(nullable = false, length = 100)
    private String department;

    /**
     * 교과목명
     */
    @Column(name = "course_name", length = 200)
    private String courseName;

    /**
     * 교수명
     */
    @Column(length = 50)
    private String professor;

    /**
     * 보강 날짜
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * 보강 시작 시간
     * - 교시 단위가 아닌 실제 시간 직접 저장 (보강일정 시트 구조 반영)
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * 보강 종료 시간
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * 필요 소프트웨어 원문
     * - 예: "Adobe Photoshop 2022"
     */
    @Column(name = "software_note", columnDefinition = "TEXT")
    private String softwareNote;

    /**
     * 사용 목적
     */
    @Column(columnDefinition = "TEXT")
    private String purpose;

    /**
     * 기타 메모
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    // ========== 생성 메서드 ==========

    public static Makeup of(Classroom classroom,
                            String department, String courseName, String professor,
                            LocalDate date, LocalTime startTime, LocalTime endTime,
                            String softwareNote, String purpose, String note) {
        Makeup makeup = new Makeup();
        makeup.classroom = classroom;
        makeup.department = department;
        makeup.courseName = courseName;
        makeup.professor = professor;
        makeup.date = date;
        makeup.startTime = startTime;
        makeup.endTime = endTime;
        makeup.softwareNote = softwareNote;
        makeup.purpose = purpose;
        makeup.note = note;
        return makeup;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 강의실 변경
     */
    public void reassignClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    /**
     * 보강 정보 수정
     */
    public void update(LocalDate date, LocalTime startTime, LocalTime endTime,
                       String softwareNote, String note) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.softwareNote = softwareNote;
        this.note = note;
    }
}