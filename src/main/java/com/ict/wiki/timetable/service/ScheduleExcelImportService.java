package com.ict.wiki.timetable.service;

import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import com.ict.wiki.timetable.domain.*;
import com.ict.wiki.timetable.repository.PeriodRepository;
import com.ict.wiki.timetable.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 완성된 시간표 엑셀 업로드 → Schedule 일괄 저장
 * - '강의실 배정' 시트만 참조
 * - 강의실까지 배정된 완성본 기준
 * - 기존 학기 데이터 삭제 후 재등록
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleExcelImportService {

    private static final String SHEET_NAME = "강의실 배정";

    private final ScheduleRepository scheduleRepository;
    private final PeriodRepository periodRepository;
    private final ClassroomService classroomService;

    /**
     * 완성된 시간표 엑셀 업로드
     * - '강의실 배정' 시트 파싱
     * - 기존 학기 데이터 삭제 후 재등록
     */
    @Transactional
    public List<Schedule> importFromTimetableExcel(MultipartFile file, String semester) {
        validateFile(file);

        List<Schedule> schedules = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw TimetableException.of(TimetableErrorCode.EXCEL_SHEET_NOT_FOUND);
            }

            String lastDepartment = null;
            Integer lastPriority = null;

            for (Row row : sheet) {
                // 첫 번째 행(헤더) 스킵
                if (row.getRowNum() == 0) continue;

                // 교과목명이 없으면 데이터 끝
                String courseName = getCellStringValue(row.getCell(4));
                if (courseName == null || courseName.isBlank()) break;

                // 강의실 셀이 없으면 스킵 (미배정)
                Cell classroomCell = row.getCell(10);
                String classroomValue = getCellStringValue(classroomCell);
                if (classroomValue == null || classroomValue.isBlank()) continue;

                try {
                    // 학과: null이면 이전 행 학과 이어받음
                    String department = getCellStringValue(row.getCell(1));
                    if (department == null || department.isBlank()) {
                        department = lastDepartment;
                    } else {
                        lastDepartment = department;
                    }

                    // 순위
                    String priorityStr = getCellStringValue(row.getCell(0));
                    if (priorityStr != null && !priorityStr.isBlank()) {
                        try {
                            lastPriority = Integer.parseInt(priorityStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    Schedule schedule = parseRow(row, semester, department, lastPriority);
                    if (schedule != null) schedules.add(schedule);

                } catch (Exception e) {
                    log.warn("행 파싱 실패 - row: {}, error: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw TimetableException.of(TimetableErrorCode.EXCEL_PARSE_FAILED);
        }

        // 기존 학기 데이터 삭제 후 재등록
        scheduleRepository.deleteBySemester(semester);
        List<Schedule> saved = scheduleRepository.saveAll(schedules);
        log.info("시간표 엑셀 업로드 완료 - semester: {}, {}건 저장", semester, saved.size());
        return saved;
    }

    private Schedule parseRow(Row row, String semester, String department, Integer priority) {
        Integer grade = getCellIntValue(row.getCell(2));
        String section = getCellStringValue(row.getCell(3));
        String courseName = getCellStringValue(row.getCell(4));
        String professor = getCellStringValue(row.getCell(5));
        String softwareNote = getCellStringValue(row.getCell(6));
        String optionStr = getCellStringValue(row.getCell(8));
        String roomNumberStr = getCellStringValue(row.getCell(10));
        String periodTypeStr = getCellStringValue(row.getCell(11));
        String dayStr = getCellStringValue(row.getCell(12));
        String periodStr = getCellStringValue(row.getCell(13));

        if (department == null || courseName == null || roomNumberStr == null
                || dayStr == null || periodStr == null) return null;

        // 강의실 조회
        Integer roomNumber;
        try {
            roomNumber = Integer.parseInt(roomNumberStr.trim());
        } catch (NumberFormatException e) {
            log.warn("강의실 번호 파싱 실패: {}", roomNumberStr);
            return null;
        }

        Classroom classroom;
        try {
            classroom = classroomService.findByRoomNumber(roomNumber);
        } catch (Exception e) {
            log.warn("강의실 없음 - roomNumber: {}, 스킵", roomNumber);
            return null;
        }

        DayOfWeek dayOfWeek = parseDayOfWeek(dayStr);
        PeriodType periodType = "야".equals(periodTypeStr) ? PeriodType.NIGHT : PeriodType.DAY;

        int[] periods = parsePeriod(periodStr);
        int periodStart = periods[0];
        int periodEnd = periods[1];

        LocalTime startTime = getStartTime(periodType, periodStart);
        LocalTime endTime = getEndTime(periodType, periodEnd);

        boolean hasOption = "O".equalsIgnoreCase(optionStr) || "○".equals(optionStr);

        return Schedule.of(
                semester, classroom, department, grade, section,
                courseName, professor, dayOfWeek, periodType,
                periodStart, periodEnd, startTime, endTime,
                softwareNote, hasOption, null, priority
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
        if (periodStr == null || !periodStr.contains("~"))
            throw new IllegalArgumentException("교시 형식 오류: " + periodStr);
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

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case FORMULA -> {
                try { yield String.valueOf((int) cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue().trim(); }
            }
            default -> null;
        };
    }

    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        try { return Integer.parseInt(getCellStringValue(cell)); }
        catch (Exception e) { return null; }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw TimetableException.of(TimetableErrorCode.EXCEL_EMPTY);
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx"))
            throw TimetableException.of(TimetableErrorCode.EXCEL_INVALID_FORMAT);
    }
}