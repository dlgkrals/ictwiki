package com.ict.wiki.timetable.domain;

import com.ict.wiki.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 정규 시간표
 * - 학기 중 매주 반복되는 정규 수업
 * - 강의실 배정 시트 엑셀 업로드 또는 직접 입력으로 등록
 * - 보강 배정 시 충돌 체크 기준
 * - 교무처 제출용 엑셀 내보내기 대상
 */
@Entity
@Table(name = "schedules",
        indexes = {
                @Index(name = "idx_schedule_classroom_day", columnList = "classroom_id, day_of_week")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 학기
     * - 예: "2025-2"
     */
    @Column(nullable = false, length = 20)
    private String semester;

    /**
     * 배정 강의실
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    /**
     * 신청 학과
     */
    @Column(nullable = false, length = 100)
    private String department;

    /**
     * 학년
     */
    @Column
    private Integer grade;

    /**
     * 반
     * - 예: "A", "B"
     */
    @Column(length = 10)
    private String section;

    /**
     * 교과목명
     */
    @Column(name = "course_name", nullable = false, length = 200)
    private String courseName;

    /**
     * 교수명
     */
    @Column(nullable = false, length = 50)
    private String professor;

    /**
     * 요일
     * - 예: MON, TUE, WED, THU, FRI
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /**
     * 주간/야간 구분
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 10)
    private PeriodType periodType;

    /**
     * 시작 교시 번호
     * - 예: 1 (1교시부터)
     */
    @Column(name = "period_start", nullable = false)
    private Integer periodStart;

    /**
     * 종료 교시 번호
     * - 예: 3 (3교시까지)
     */
    @Column(name = "period_end", nullable = false)
    private Integer periodEnd;

    /**
     * 실제 수업 시작 시간 (Period 테이블에서 계산하여 저장)
     * - 충돌 체크 시 직접 사용
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * 실제 수업 종료 시간 (Period 테이블에서 계산하여 저장)
     * - 충돌 체크 시 직접 사용
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * 신청 소프트웨어 원문 (엑셀 입력값 그대로 보관)
     * - 예: "Adobe Photoshop 2022, 한글 2022"
     */
    @Column(name = "software_note", columnDefinition = "TEXT")
    private String softwareNote;

    /**
     * 특수 옵션 여부
     * - true: 세부 설치 설정 필요 (VirtualBox에 Ubuntu 설치 등)
     */
    @Column(name = "has_option", nullable = false)
    private boolean hasOption = false;

    /**
     * 옵션 상세 내용
     * - 예: "VirtualBox에 Ubuntu 22.04 미리 설치"
     */
    @Column(name = "option_note", columnDefinition = "TEXT")
    private String optionNote;

    /**
     * 배정 순위 (선착순)
     * - 같은 시간대 충돌 시 순위가 낮은 쪽이 조정 대상
     */
    @Column
    private Integer priority;

    // ========== 생성 메서드 ==========

    public static Schedule of(String semester, Classroom classroom,
                              String department, Integer grade, String section,
                              String courseName, String professor,
                              DayOfWeek dayOfWeek, PeriodType periodType,
                              int periodStart, int periodEnd,
                              LocalTime startTime, LocalTime endTime,
                              String softwareNote, boolean hasOption, String optionNote,
                              Integer priority) {
        Schedule schedule = new Schedule();
        schedule.semester = semester;
        schedule.classroom = classroom;
        schedule.department = department;
        schedule.grade = grade;
        schedule.section = section;
        schedule.courseName = courseName;
        schedule.professor = professor;
        schedule.dayOfWeek = dayOfWeek;
        schedule.periodType = periodType;
        schedule.periodStart = periodStart;
        schedule.periodEnd = periodEnd;
        schedule.startTime = startTime;
        schedule.endTime = endTime;
        schedule.softwareNote = softwareNote;
        schedule.hasOption = hasOption;
        schedule.optionNote = optionNote;
        schedule.priority = priority;
        return schedule;
    }

    public void update(Classroom classroom,
                       String department, Integer grade, String section,
                       String courseName, String professor,
                       DayOfWeek dayOfWeek, PeriodType periodType,
                       int periodStart, int periodEnd,
                       LocalTime startTime, LocalTime endTime,
                       String softwareNote, boolean hasOption, String optionNote,
                       Integer priority) {
        this.classroom = classroom;
        this.department = department;
        this.grade = grade;
        this.section = section;
        this.courseName = courseName;
        this.professor = professor;
        this.dayOfWeek = dayOfWeek;
        this.periodType = periodType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.startTime = startTime;
        this.endTime = endTime;
        this.softwareNote = softwareNote;
        this.hasOption = hasOption;
        this.optionNote = optionNote;
        this.priority = priority;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 강의실 변경 (충돌로 인한 재배정 시)
     */
    public void reassignClassroom(Classroom classroom) {
        this.classroom = classroom;
    }
}