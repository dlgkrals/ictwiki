package com.ict.wiki.timetable.service;

import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import com.ict.wiki.timetable.domain.Classroom;
import com.ict.wiki.timetable.domain.DayOfWeek;
import com.ict.wiki.timetable.domain.Makeup;
import com.ict.wiki.timetable.domain.Schedule;
import com.ict.wiki.timetable.dto.request.MakeupCreateRequest;
import com.ict.wiki.timetable.dto.response.MakeupResponse;
import com.ict.wiki.timetable.repository.MakeupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 보강 서비스
 * - 보강 CRUD
 * - 배정 가능 강의실 추천 (소프트웨어 조건 + 충돌 체크)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MakeupService {

    private final MakeupRepository makeupRepository;
    private final ClassroomService classroomService;
    private final ScheduleService scheduleService;

    // ========== 조회 ==========

    /**
     * 달력 뷰용 - 월간 보강 목록
     */
    public List<Makeup> getByMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return makeupRepository.findByDateBetween(start, end);
    }

    /**
     * 날짜 범위 조회
     */
    public List<Makeup> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return makeupRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * 단건 조회
     */
    public Makeup findById(Long id) {
        return makeupRepository.findByIdWithClassroom(id)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.MAKEUP_NOT_FOUND));
    }

    // ========== 배정 가능 강의실 추천 ==========

    /**
     * 조건에 맞는 배정 가능 강의실 목록 반환
     * 조건 1: 필요 소프트웨어 보유
     * 조건 2: 해당 날짜 + 시간에 정규수업 없음
     * 조건 3: 해당 날짜 + 시간에 기존 보강 없음
     */
    public List<Classroom> findAvailableClassrooms(LocalDate date, LocalTime startTime, LocalTime endTime,
                                                   List<Long> softwareIds) {
        // 소프트웨어 조건 충족 강의실 필터링
        List<Classroom> candidates = classroomService.findAvailableClassrooms(softwareIds);

        // 요일 변환
        DayOfWeek dayOfWeek = toDayOfWeek(date);

        // 현재 학기 (실제로는 파라미터로 받거나 설정값에서 가져와야 함 - 우선 하드코딩)
        String semester = getCurrentSemester();

        // 정규수업 + 보강 충돌 제거
        return candidates.stream()
                .filter(classroom -> {
                    // 정규수업 충돌 체크
                    List<Schedule> scheduleConflicts = scheduleService.checkConflicts(
                            semester, classroom.getId(), dayOfWeek, startTime, endTime);
                    if (!scheduleConflicts.isEmpty()) return false;

                    // 보강 충돌 체크
                    List<Makeup> makeupConflicts = makeupRepository.findConflicts(
                            classroom.getId(), date, startTime, endTime);
                    return makeupConflicts.isEmpty();
                })
                .toList();
    }

    // ========== 등록/수정/삭제 ==========

    /**
     * 보강 등록
     * - 강의실 배정 후 최종 충돌 재확인
     */
    @Transactional
    public Makeup createMakeup(Long classroomId, String department, String courseName, String professor,
                               LocalDate date, LocalTime startTime, LocalTime endTime,
                               String softwareNote, String purpose, String note) {

        Classroom classroom = classroomService.findById(classroomId);
        DayOfWeek dayOfWeek = toDayOfWeek(date);
        String semester = getCurrentSemester();

        // 정규수업 충돌 최종 확인
        List<Schedule> scheduleConflicts = scheduleService.checkConflicts(
                semester, classroomId, dayOfWeek, startTime, endTime);
        if (!scheduleConflicts.isEmpty()) {
            throw TimetableException.of(TimetableErrorCode.MAKEUP_SCHEDULE_CONFLICT);
        }

        // 보강 충돌 최종 확인
        List<Makeup> makeupConflicts = makeupRepository.findConflicts(
                classroomId, date, startTime, endTime);
        if (!makeupConflicts.isEmpty()) {
            throw TimetableException.of(TimetableErrorCode.MAKEUP_CONFLICT);
        }

        Makeup makeup = Makeup.of(classroom, department, courseName, professor,
                date, startTime, endTime, softwareNote, purpose, note);

        log.info("보강 등록 - {}호 {} {} ~ {} [{}]",
                classroom.getRoomNumber(), date, startTime, endTime, department);
        return makeupRepository.save(makeup);
    }

    @Transactional
    public List<MakeupResponse> createMakeups(List<MakeupCreateRequest> requests) {
        List<Makeup> makeups = requests.stream()
                .map(req -> {
                    Classroom classroom = classroomService.findById(req.getClassroomId());
                    return Makeup.of(classroom, req.getDepartment(), req.getCourseName(),
                            req.getProfessor(), req.getDate(), req.getStartTime(),
                            req.getEndTime(), req.getSoftwareNote(), req.getPurpose(), req.getNote());
                })
                .toList();
        return makeupRepository.saveAll(makeups).stream()
                .map(MakeupResponse::from)
                .toList();
    }

    /**
     * 보강 수정
     */
    @Transactional
    public Makeup updateMakeup(Long id, LocalDate date, LocalTime startTime, LocalTime endTime,
                               String softwareNote, String note) {
        Makeup makeup = findById(id);

        // 충돌 체크 (자기 자신 제외)
        List<Makeup> conflicts = makeupRepository.findConflictsExcluding(
                makeup.getClassroom().getId(), date, startTime, endTime, id);
        if (!conflicts.isEmpty()) {
            throw TimetableException.of(TimetableErrorCode.MAKEUP_CONFLICT);
        }

        makeup.update(date, startTime, endTime, softwareNote, note);
        return makeup;
    }

    /**
     * 보강 삭제
     */
    @Transactional
    public void deleteMakeup(Long id) {
        Makeup makeup = findById(id);
        makeupRepository.delete(makeup);
        log.info("보강 삭제 - id: {}, 강의실: {}호, 날짜: {}", id, makeup.getClassroom().getRoomNumber(), makeup.getDate());
    }

    // ========== 내부 유틸 ==========

    private DayOfWeek toDayOfWeek(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> DayOfWeek.MON;
            case TUESDAY -> DayOfWeek.TUE;
            case WEDNESDAY -> DayOfWeek.WED;
            case THURSDAY -> DayOfWeek.THU;
            case FRIDAY -> DayOfWeek.FRI;
            default -> throw TimetableException.of(TimetableErrorCode.MAKEUP_WEEKEND);
        };
    }

    /**
     * 현재 학기 반환 (날짜 기반 자동 계산)
     * 1~6월 → 1학기, 7~12월 → 2학기
     */
    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) return year + "-1";
        return year + "-2";
    }
}