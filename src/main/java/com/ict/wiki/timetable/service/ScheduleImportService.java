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
 * 신청서 엑셀 업로드 → 미배정 Schedule 파싱/저장 서비스
 *
 * 신청서 컬럼 순서 (0-based index):
 * 0: 번호
 * 1: 학과
 * 2: 학년
 * 3: 교과목
 * 4: 설치 소프트웨어
 * 5: OS
 * 6: 요일
 * 7: 교시 (예: "1~3", "5~7")
 * 8: 주/야
 * 9: 반
 * 10: 사용인원
 * 11: 신청교수명
 * 12: 설치옵션 여부 (O/X)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleImportService {

    private final ScheduleRepository scheduleRepository;
    private final PeriodRepository periodRepository;

    /**
     * 신청서 엑셀 업로드 → 미배정 Schedule 파싱 후 저장
     * - 강의실은 null (미배정 상태)
     * - 충돌 체크 없이 저장 (배정 전 단계)
     */
    @Transactional
    public List<Schedule> importFromApplication(MultipartFile file, String semester) {
        validateFile(file);

        List<Schedule> schedules = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // 1행: 제목, 2행: 헤더 스킵
                if (row.getRowNum() < 3) continue;

                // 번호 셀이 비어있으면 데이터 끝
                Cell numberCell = row.getCell(0);
                if (numberCell == null || getCellStringValue(numberCell).isBlank()) break;

                // 번호가 숫자가 아닌 경우 (작성자, 주요항목 행 등) 스킵
                if (numberCell.getCellType() != CellType.NUMERIC) break;

                try {
                    Schedule schedule = parseRow(row, semester);
                    if (schedule != null) schedules.add(schedule);
                } catch (Exception e) {
                    log.warn("행 파싱 실패 - row: {}, error: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw TimetableException.of(TimetableErrorCode.EXCEL_PARSE_FAILED);
        }

        List<Schedule> saved = scheduleRepository.saveAll(schedules);
        log.info("신청서 업로드 완료 - semester: {}, 파싱 {}건 저장", semester, saved.size());
        return saved;
    }

    /**
     * 행 하나 → Schedule 변환
     */
    private Schedule parseRow(Row row, String semester) {
        String department = getCellStringValue(row.getCell(1));
        if (department == null || department.isBlank()) return null;

        Integer grade = getCellIntValue(row.getCell(2));
        String courseName = getCellStringValue(row.getCell(3));
        String softwareNote = getCellStringValue(row.getCell(4));
        String dayStr = getCellStringValue(row.getCell(6));
        String periodStr = getCellStringValue(row.getCell(7));  // "1~3", "5~7"
        String periodTypeStr = getCellStringValue(row.getCell(8)); // "주", "야"
        String section = getCellStringValue(row.getCell(9));
        String professor = getCellStringValue(row.getCell(11));
        String optionStr = getCellStringValue(row.getCell(12)); // "O", "X"

        // 요일 변환
        DayOfWeek dayOfWeek = parseDayOfWeek(dayStr);

        // 주/야 변환
        PeriodType periodType = "야".equals(periodTypeStr) ? PeriodType.NIGHT : PeriodType.DAY;

        // 교시 파싱 "1~3" → start=1, end=3
        int[] periods = parsePeriod(periodStr);
        int periodStart = periods[0];
        int periodEnd = periods[1];

        // 교시 → 실제 시간 변환
        LocalTime startTime = getStartTime(periodType, periodStart);
        LocalTime endTime = getEndTime(periodType, periodEnd);

        // 설치옵션 여부
        boolean hasOption = "O".equalsIgnoreCase(optionStr) || "○".equals(optionStr);

        // 강의실은 null (미배정)
        return Schedule.of(
                semester, null, department, grade, section,
                courseName, professor, dayOfWeek, periodType,
                periodStart, periodEnd, startTime, endTime,
                softwareNote, hasOption, null, null
        );
    }

    // ========== 파싱 유틸 ==========

    private DayOfWeek parseDayOfWeek(String day) {
        if (day == null) throw new IllegalArgumentException("요일이 비어있습니다.");
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
        // "1~3", "5~7", "8~10" 형식 파싱
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

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        try {
            return Integer.parseInt(getCellStringValue(cell));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw TimetableException.of(TimetableErrorCode.EXCEL_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw TimetableException.of(TimetableErrorCode.EXCEL_INVALID_FORMAT);
        }
    }
}