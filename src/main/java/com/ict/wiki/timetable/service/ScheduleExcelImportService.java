package com.ict.wiki.timetable.service;

import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import com.ict.wiki.timetable.domain.*;
import com.ict.wiki.timetable.dto.request.ScheduleExcelImportRequest;
import com.ict.wiki.timetable.repository.PeriodRepository;
import com.ict.wiki.timetable.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleExcelImportService {

    private final ScheduleRepository scheduleRepository;
    private final PeriodRepository periodRepository;
    private final ClassroomService classroomService;

    @Transactional
    public List<Schedule> importFromTimetableExcel(
            List<ScheduleExcelImportRequest> rows, String semester) {

        List<Schedule> schedules = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            try {
                Schedule schedule = parseRow(rows.get(i), semester);
                if (schedule != null) schedules.add(schedule);
            } catch (Exception e) {
                log.warn("행 파싱 실패 - index: {}, error: {}", i, e.getMessage());
            }
        }

        scheduleRepository.deleteBySemester(semester);
        List<Schedule> saved = scheduleRepository.saveAll(schedules);
        log.info("시간표 엑셀 업로드 완료 - semester: {}, {}건 저장", semester, saved.size());
        return saved;
    }

    private Schedule parseRow(ScheduleExcelImportRequest req, String semester) {
        if (req.getDepartment() == null || req.getCourseName() == null
                || req.getRoomNumber() == null || req.getDayOfWeek() == null
                || req.getPeriodStr() == null) return null;

        Classroom classroom;
        try {
            classroom = classroomService.findByRoomNumber(req.getRoomNumber());
        } catch (Exception e) {
            log.warn("강의실 없음 - roomNumber: {}, 스킵", req.getRoomNumber());
            return null;
        }

        DayOfWeek dayOfWeek = parseDayOfWeek(req.getDayOfWeek());
        PeriodType periodType = "야".equals(req.getPeriodType()) ? PeriodType.NIGHT : PeriodType.DAY;

        int[] periods = parsePeriod(req.getPeriodStr());
        int periodStart = periods[0];
        int periodEnd = periods[1];

        LocalTime startTime = getStartTime(periodType, periodStart);
        LocalTime endTime = getEndTime(periodType, periodEnd);

        boolean hasOption = "O".equalsIgnoreCase(req.getOptionStr())
                || "○".equals(req.getOptionStr());

        return Schedule.of(
                semester, classroom, req.getDepartment(), req.getGrade(), req.getSection(),
                req.getCourseName(), req.getProfessor(), dayOfWeek, periodType,
                periodStart, periodEnd, startTime, endTime,
                req.getSoftwareNote(), hasOption, null, req.getPriority()
        );
    }

    // ========== 파싱 유틸 ==========

    private DayOfWeek parseDayOfWeek(String day) {
        return switch (day.trim()) {
            case "월" -> DayOfWeek.MON;
            case "화" -> DayOfWeek.TUE;
            case "수" -> DayOfWeek.WED;
            case "목" -> DayOfWeek.THU;
            case "금" -> DayOfWeek.FRI;
            default -> throw new IllegalArgumentException("알 수 없는 요일: " + day);
        };
    }

    private int[] parsePeriod(String periodStr) {
        if (periodStr == null || !periodStr.contains("~")) {
            throw new IllegalArgumentException("교시 형식 오류: " + periodStr);
        }
        String[] parts = periodStr.trim().split("~");
        return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
    }

    private LocalTime getStartTime(PeriodType type, int periodNumber) {
        return periodRepository.findByTypeAndPeriodNumber(type, periodNumber)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.PERIOD_NOT_FOUND,
                        type.getLabel() + periodNumber))
                .getStartTime();
    }

    private LocalTime getEndTime(PeriodType type, int periodNumber) {
        return periodRepository.findByTypeAndPeriodNumber(type, periodNumber)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.PERIOD_NOT_FOUND,
                        type.getLabel() + periodNumber))
                .getEndTime();
    }
}