package com.ict.wiki.timetable.service;

import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import com.ict.wiki.timetable.domain.*;
import com.ict.wiki.timetable.dto.request.ScheduleUpdateRequest;
import com.ict.wiki.timetable.repository.PeriodRepository;
import com.ict.wiki.timetable.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * 정규 시간표 서비스
 * - 시간표 직접 입력 / 엑셀 업로드 (파싱은 ExcelService에서 담당)
 * - 충돌 체크 (강의실 + 요일 + 시간 겹침)
 * - 교무처용 엑셀 내보내기 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final PeriodRepository periodRepository;
    private final ClassroomService classroomService;

    // ========== 조회 ==========

    /**
     * 학기 전체 시간표
     */
    public List<Schedule> getBySemester(String semester) {
        return scheduleRepository.findBySemester(semester);
    }

    /**
     * 학기 + 강의실별 시간표
     */
    public List<Schedule> getBySemesterAndClassroom(String semester, Long classroomId) {
        return scheduleRepository.findBySemesterAndClassroomId(semester, classroomId);
    }

    /**
     * 미배정 수업 목록 조회 (강의실 없는 것)
     */
    public List<Schedule> getUnassigned(String semester) {
        return scheduleRepository.findUnassignedBySemester(semester);
    }

    /**
     * 미배정 수업에 강의실 배정
     * - 충돌 체크 후 배정
     */
    @Transactional
    public Schedule assignClassroom(Long id, Long classroomId) {
        Schedule schedule = findById(id);
        Classroom classroom = classroomService.findById(classroomId);

        // 충돌 체크
        List<Schedule> conflicts = scheduleRepository.findConflictsExcluding(
                schedule.getSemester(), classroomId,
                schedule.getDayOfWeek(), schedule.getStartTime(), schedule.getEndTime(), id);
        if (!conflicts.isEmpty()) {
            throw TimetableException.of(TimetableErrorCode.SCHEDULE_CONFLICT,
                    buildConflictMessage(conflicts));
        }

        schedule.reassignClassroom(classroom);
        log.info("강의실 배정 - scheduleId: {}, 강의실: {}호 [{}]",
                id, classroom.getRoomNumber(), schedule.getCourseName());
        return schedule;
    }

    /**
     * 단건 조회
     */
    public Schedule findById(Long id) {
        return scheduleRepository.findByIdWithClassroom(id)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.SCHEDULE_NOT_FOUND));
    }

    // ========== 등록 ==========

    /**
     * 수업 단건 등록 (직접 입력)
     * - 저장 전 충돌 체크 수행
     */
    @Transactional
    public Schedule createSchedule(String semester, Long classroomId,
                                   String department, Integer grade, String section,
                                   String courseName, String professor,
                                   DayOfWeek dayOfWeek, PeriodType periodType,
                                   int periodStart, int periodEnd,
                                   String softwareNote, boolean hasOption, String optionNote,
                                   Integer priority) {

        Classroom classroom = (classroomId != null) ? classroomService.findById(classroomId) : null;

        // 교시 → 실제 시간 변환
        LocalTime startTime = getStartTime(periodType, periodStart);
        LocalTime endTime = getEndTime(periodType, periodEnd);

        // 충돌 체크 - 강의실 배정된 경우에만
        if (classroom != null) {
            List<Schedule> conflicts = scheduleRepository.findConflicts(
                    semester, classroomId, dayOfWeek, startTime, endTime);
            if (!conflicts.isEmpty()) {
                throw TimetableException.of(TimetableErrorCode.SCHEDULE_CONFLICT, buildConflictMessage(conflicts));
            }
        }

        Schedule schedule = Schedule.of(
                semester, classroom, department, grade, section,
                courseName, professor, dayOfWeek, periodType,
                periodStart, periodEnd, startTime, endTime,
                softwareNote, hasOption, optionNote, priority
        );

        log.info("시간표 등록 - {} {} {} {}~{}교시 [{}]",
                classroom != null ? classroom.getRoomNumber() + "호" : "미배정",
                dayOfWeek.getLabel(), periodType.getLabel(), periodStart, periodEnd, courseName);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule updateSchedule(Long id, ScheduleUpdateRequest req) {

        Schedule schedule = findById(id);

        Classroom classroom;
        if (Boolean.TRUE.equals(req.getClearClassroom())) {
            classroom = null;
        } else if (req.getClassroomId() != null) {
            classroom = classroomService.findById(req.getClassroomId());
        } else {
            classroom = schedule.getClassroom();
        }

        PeriodType resolvedType = req.getPeriodType() != null ? req.getPeriodType() : schedule.getPeriodType();
        int resolvedStart = req.getPeriodStart() != null ? req.getPeriodStart() : schedule.getPeriodStart();
        int resolvedEnd = req.getPeriodEnd() != null ? req.getPeriodEnd() : schedule.getPeriodEnd();

        LocalTime startTime = getStartTime(resolvedType, resolvedStart);
        LocalTime endTime = getEndTime(resolvedType, resolvedEnd);

        Long resolvedClassroomId = classroom != null ? classroom.getId()
                : (schedule.getClassroom() != null ? schedule.getClassroom().getId() : null);
        DayOfWeek resolvedDay = req.getDayOfWeek() != null ? req.getDayOfWeek() : schedule.getDayOfWeek();

        if (resolvedClassroomId != null) {
            List<Schedule> conflicts = scheduleRepository.findConflictsExcluding(
                    schedule.getSemester(), resolvedClassroomId,
                    resolvedDay, startTime, endTime, id);
            if (!conflicts.isEmpty()) {
                throw TimetableException.of(TimetableErrorCode.SCHEDULE_CONFLICT,
                        buildConflictMessage(conflicts));
            }
        }

        schedule.update(
                classroom,
                req.getDepartment() != null ? req.getDepartment() : schedule.getDepartment(),
                req.getGrade() != null ? req.getGrade() : schedule.getGrade(),
                req.getSection() != null ? req.getSection() : schedule.getSection(),
                req.getCourseName() != null ? req.getCourseName() : schedule.getCourseName(),
                req.getProfessor() != null ? req.getProfessor() : schedule.getProfessor(),
                resolvedDay, resolvedType,
                resolvedStart, resolvedEnd,
                startTime, endTime,
                req.getSoftwareNote() != null ? req.getSoftwareNote() : schedule.getSoftwareNote(),
                req.getHasOption() != null ? req.getHasOption() : schedule.isHasOption(),
                req.getOptionNote() != null ? req.getOptionNote() : schedule.getOptionNote(),
                req.getPriority() != null ? req.getPriority() : schedule.getPriority()
        );

        log.info("시간표 수정 - id: {}, 과목: {}", id, schedule.getCourseName());
        return schedule;
    }

    /**
     * 일괄 등록 (엑셀 업로드 시 ExcelService에서 파싱 후 호출)
     * - 학기 기존 데이터 삭제 후 재등록
     */
    @Transactional
    public List<Schedule> saveAll(String semester, List<Schedule> schedules) {
        scheduleRepository.deleteBySemester(semester);
        List<Schedule> saved = scheduleRepository.saveAll(schedules);
        log.info("시간표 일괄 저장 - semester: {}, 총 {}건", semester, saved.size());
        return saved;
    }

    /**
     * 수업 삭제
     */
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = findById(id);
        scheduleRepository.delete(schedule);
        log.info("시간표 삭제 - id: {}, 강의실: {}", id, schedule.getClassroom() != null ? schedule.getClassroom().getRoomNumber() + "호" : "미배정");
    }

    /**
     * 학기 전체 삭제 (재업로드 시)
     */
    @Transactional
    public void deleteBySemester(String semester) {
        scheduleRepository.deleteBySemester(semester);
        log.info("학기 시간표 전체 삭제 - semester: {}", semester);
    }

    public List<String> getAllSemesters() {
        return scheduleRepository.findAllSemesters();
    }
    // ========== 충돌 체크 (외부에서도 사용) ==========

    /**
     * 충돌 여부 확인
     * - 보강 배정 시 정규수업과의 충돌 체크에도 사용
     */
    public List<Schedule> checkConflicts(String semester, Long classroomId,
                                         DayOfWeek dayOfWeek,
                                         LocalTime startTime, LocalTime endTime) {
        return scheduleRepository.findConflicts(semester, classroomId, dayOfWeek, startTime, endTime);
    }

    // ========== 내부 유틸 ==========

    /**
     * 교시 번호 → 시작 시간
     */
    public LocalTime getStartTime(PeriodType type, int periodNumber) {
        return periodRepository.findByTypeAndPeriodNumber(type, periodNumber)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.PERIOD_NOT_FOUND,
                        type.getLabel() + periodNumber))
                .getStartTime();
    }

    /**
     * 교시 번호 → 종료 시간
     */
    public LocalTime getEndTime(PeriodType type, int periodNumber) {
        return periodRepository.findByTypeAndPeriodNumber(type, periodNumber)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.PERIOD_NOT_FOUND,
                        type.getLabel() + periodNumber))
                .getEndTime();
    }

    private String buildConflictMessage(List<Schedule> conflicts) {
        Schedule first = conflicts.get(0);
        return String.format("시간 충돌: %s %s %s%d~%d교시에 이미 '%s' 수업이 배정되어 있습니다.",
                first.getClassroom().getRoomNumber() + "호",
                first.getDayOfWeek().getLabel(),
                first.getPeriodType().getLabel(),
                first.getPeriodStart(),
                first.getPeriodEnd(),
                first.getCourseName());
    }
}