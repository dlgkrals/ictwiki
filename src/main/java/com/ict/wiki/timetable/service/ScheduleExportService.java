package com.ict.wiki.timetable.service;

import com.ict.wiki.timetable.domain.DayOfWeek;
import com.ict.wiki.timetable.domain.PeriodType;
import com.ict.wiki.timetable.domain.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleExportService {

    private final ScheduleService scheduleService;

    private static final int[] FLOOR2_ROOMS = {201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 215};
    private static final int[] FLOOR3_ROOMS = {301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 315};
    private static final int[] FLOOR4_ROOMS = {410, 411, 412, 413};

    private static final String[] DAY_PERIODS = {
            "1교시(09:00～09:50)", "2교시(10:00～10:50)", "3교시(11:00～11:50)",
            "4교시(12:00～12:50)", "5교시(13:00～13:50)", "6교시(14:00～14:50)",
            "7교시(15:00～15:50)", "8교시(16:00～16:50)", "9교시(17:00～17:50)",
            "10교시(18:00～18:50)"
    };

    private static final String[] NIGHT_PERIODS = {
            "1교시(17:30～18:15)", "2교시(18:15～19:00)", "3교시(19:05～19:50)",
            "4교시(19:50～20:35)", "5교시(20:40～21:25)", "6교시(21:25～22:10)"
    };

    private static final DayOfWeek[] DAYS = {
            DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI
    };

    // 주간 4교시(index 3)는 단독 - 점심시간
    private static final int DAY_LUNCH_IDX = 3;

    // 야간 병합 구간: 0~2 (1~3교시), 3~5 (4~6교시)
    private static final int[][] NIGHT_GROUPS = {{0, 1, 2}, {3, 4, 5}};

    private static final int SEC2F_COL = 0;
    private static final int SEC3F_COL = 17;
    private static final int SEC4F_COL = 34;

    private static final byte[] DATA_COLOR = {(byte)0xFF, (byte)0xF2, (byte)0xCC};
    private static final byte[] NIGHT_LABEL_COLOR = {(byte)0xFF, (byte)0xFF, (byte)0x00};

    public byte[] exportToExcel(String semester) throws IOException {
        List<Schedule> schedules = scheduleService.getBySemester(semester);

        Map<String, Schedule> scheduleMap = new LinkedHashMap<>();
        for (Schedule s : schedules) {
            if (s.getClassroom() == null) continue;
            String key = makeKey(s.getClassroom().getRoomNumber(), s.getDayOfWeek(), s.getPeriodType(), s.getPeriodStart());
            scheduleMap.put(key, s);
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("2F공용PC실습실");

            XSSFCellStyle headerStyle     = makeHeaderStyle(wb);
            XSSFCellStyle dayStyle        = makeDayStyle(wb);
            XSSFCellStyle borderStyle     = makeBorderOnlyStyle(wb);
            XSSFCellStyle timeStyle       = makeTimeStyle(wb);
            XSSFCellStyle dataFilledStyle = makeDataStyle(wb, true);
            XSSFCellStyle dataEmptyStyle  = makeDataStyle(wb, false);
            XSSFCellStyle nightLabelStyle = makeNightLabelStyle(wb);

            setColumnWidths(sheet);

            // === 주간 헤더 ===
            Row headerRow = getOrCreateRow(sheet, 0);
            headerRow.setHeightInPoints(18f);
            writeHeaders(headerRow, headerStyle);

            // === 주간 데이터 ===
            int currentRow = 1;
            for (DayOfWeek day : DAYS) {
                int dayRowStart = currentRow;
                for (int p = 0; p < DAY_PERIODS.length; p++) {
                    Row row = getOrCreateRow(sheet, currentRow + p);
                    row.setHeightInPoints(45f);

                    setCell(row, SEC2F_COL + 1, DAY_PERIODS[p], timeStyle);
                    setCell(row, SEC3F_COL + 1, DAY_PERIODS[p], timeStyle);
                    setCell(row, SEC4F_COL + 1, DAY_PERIODS[p], timeStyle);

                    writeRoomCells(row, day, PeriodType.DAY, p, FLOOR2_ROOMS, SEC2F_COL + 2, scheduleMap, dataFilledStyle, dataEmptyStyle);
                    writeRoomCells(row, day, PeriodType.DAY, p, FLOOR3_ROOMS, SEC3F_COL + 2, scheduleMap, dataFilledStyle, dataEmptyStyle);
                    writeRoomCells(row, day, PeriodType.DAY, p, FLOOR4_ROOMS, SEC4F_COL + 2, scheduleMap, dataFilledStyle, dataEmptyStyle);
                }

                writeDayCell(sheet, dayRowStart, DAY_PERIODS.length, day.getLabel(), SEC2F_COL, dayStyle, borderStyle);
                writeDayCell(sheet, dayRowStart, DAY_PERIODS.length, day.getLabel(), SEC3F_COL, dayStyle, borderStyle);
                writeDayCell(sheet, dayRowStart, DAY_PERIODS.length, day.getLabel(), SEC4F_COL, dayStyle, borderStyle);

                mergePeriods(sheet, scheduleMap, day, PeriodType.DAY, dayRowStart, FLOOR2_ROOMS, SEC2F_COL + 2, DAY_PERIODS.length);
                mergePeriods(sheet, scheduleMap, day, PeriodType.DAY, dayRowStart, FLOOR3_ROOMS, SEC3F_COL + 2, DAY_PERIODS.length);
                mergePeriods(sheet, scheduleMap, day, PeriodType.DAY, dayRowStart, FLOOR4_ROOMS, SEC4F_COL + 2, DAY_PERIODS.length);

                mergeEmptyDay(sheet, scheduleMap, day, dayRowStart, FLOOR2_ROOMS, SEC2F_COL + 2);
                mergeEmptyDay(sheet, scheduleMap, day, dayRowStart, FLOOR3_ROOMS, SEC3F_COL + 2);
                mergeEmptyDay(sheet, scheduleMap, day, dayRowStart, FLOOR4_ROOMS, SEC4F_COL + 2);

                currentRow += DAY_PERIODS.length;

                if (day != DayOfWeek.FRI) {
                    Row sepRow = getOrCreateRow(sheet, currentRow);
                    sepRow.setHeightInPoints(13f);
                    writeSepRow(sepRow, borderStyle, timeStyle, true);
                    currentRow++;
                }
            }

            // === 야간 구분선 ===
            int nightLabelRow = currentRow;
            for (int i = 0; i < 3; i++)
                getOrCreateRow(sheet, nightLabelRow + i).setHeightInPoints(18f);
            Row nightRow = getOrCreateRow(sheet, nightLabelRow);
            Cell nightLabel = nightRow.createCell(SEC2F_COL);
            nightLabel.setCellValue("2층 야간");
            nightLabel.setCellStyle(nightLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(
                    nightLabelRow, nightLabelRow + 2,
                    SEC2F_COL, SEC2F_COL + 1 + FLOOR2_ROOMS.length));
            currentRow += 3;

            // === 야간 헤더 ===
            Row nightHeaderRow = getOrCreateRow(sheet, currentRow);
            nightHeaderRow.setHeightInPoints(18f);
            writeNightHeaders(nightHeaderRow, headerStyle);
            currentRow++;

            // === 야간 데이터 ===
            for (DayOfWeek day : DAYS) {
                int dayRowStart = currentRow;
                for (int p = 0; p < NIGHT_PERIODS.length; p++) {
                    Row row = getOrCreateRow(sheet, currentRow + p);
                    row.setHeightInPoints(45f);
                    setCell(row, SEC2F_COL + 1, NIGHT_PERIODS[p], timeStyle);
                    writeRoomCells(row, day, PeriodType.NIGHT, p, FLOOR2_ROOMS, SEC2F_COL + 2, scheduleMap, dataFilledStyle, dataEmptyStyle);
                }

                writeDayCell(sheet, dayRowStart, NIGHT_PERIODS.length, day.getLabel(), SEC2F_COL, dayStyle, borderStyle);
                mergePeriods(sheet, scheduleMap, day, PeriodType.NIGHT, dayRowStart, FLOOR2_ROOMS, SEC2F_COL + 2, NIGHT_PERIODS.length);
                mergeEmptyNight(sheet, scheduleMap, day, dayRowStart, FLOOR2_ROOMS, SEC2F_COL + 2);

                currentRow += NIGHT_PERIODS.length;

                if (day != DayOfWeek.FRI) {
                    Row sepRow = getOrCreateRow(sheet, currentRow);
                    sepRow.setHeightInPoints(13f);
                    writeSepRow(sepRow, borderStyle, timeStyle, false);
                    currentRow++;
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            log.info("시간표 엑셀 내보내기 - semester: {}, 데이터: {}건", semester, scheduleMap.size());
            return out.toByteArray();
        }
    }

    /**
     * 주간 빈 칸 병합
     * - 4교시(index 3)를 경계로 앞뒤 구간 분리
     * - 각 구간에서 연속 빈 칸을 스캔, 수업 만나면 끊고 최대 3칸씩 병합
     */
    private void mergeEmptyDay(Sheet sheet, Map<String, Schedule> scheduleMap,
                               DayOfWeek day, int dayRowStart, int[] rooms, int colOffset) {
        // 구간 1: 1~3교시 (index 0~2)
        // 구간 2: 5~10교시 (index 4~9)
        int[][] daySegments = {{0, 3}, {4, DAY_PERIODS.length}};

        for (int[] seg : daySegments) {
            mergeEmptySegment(sheet, scheduleMap, day, PeriodType.DAY, dayRowStart, rooms, colOffset, seg[0], seg[1]);
        }
    }

    /**
     * 야간 빈 칸 병합
     * - 1~3교시 (index 0~2), 4~6교시 (index 3~5) 두 구간
     */
    private void mergeEmptyNight(Sheet sheet, Map<String, Schedule> scheduleMap,
                                 DayOfWeek day, int dayRowStart, int[] rooms, int colOffset) {
        for (int[] group : NIGHT_GROUPS) {
            mergeEmptySegment(sheet, scheduleMap, day, PeriodType.NIGHT, dayRowStart, rooms, colOffset, group[0], group[group.length - 1] + 1);
        }
    }

    /**
     * 구간 내 연속 빈 칸 스캔 후 최대 3칸씩 병합
     * segStart (inclusive) ~ segEnd (exclusive)
     */
    private void mergeEmptySegment(Sheet sheet, Map<String, Schedule> scheduleMap,
                                   DayOfWeek day, PeriodType periodType,
                                   int dayRowStart, int[] rooms, int colOffset,
                                   int segStart, int segEnd) {
        for (int r = 0; r < rooms.length; r++) {
            int col = colOffset + r;
            int p = segStart;
            while (p < segEnd) {
                String key = makeKey(rooms[r], day, periodType, p + 1);
                if (scheduleMap.containsKey(key)) {
                    // 수업 있으면 수업 끝까지 건너뜀
                    Schedule s = scheduleMap.get(key);
                    p += (s.getPeriodEnd() - s.getPeriodStart()) + 1;
                    continue;
                }

                // 연속 빈 칸 찾기
                int emptyStart = p;
                while (p < segEnd && !scheduleMap.containsKey(makeKey(rooms[r], day, periodType, p + 1))) {
                    p++;
                }
                int emptyLen = p - emptyStart;

                // 최대 3칸씩 병합
                int idx = emptyStart;
                while (idx < emptyStart + emptyLen) {
                    int blockSize = Math.min(3, emptyStart + emptyLen - idx);
                    if (blockSize >= 2) {
                        try {
                            sheet.addMergedRegion(new CellRangeAddress(
                                    dayRowStart + idx,
                                    dayRowStart + idx + blockSize - 1,
                                    col, col));
                        } catch (Exception ignored) {}
                    }
                    idx += blockSize;
                }
            }
        }
    }

    private void writeDayCell(Sheet sheet, int startRow, int periodCount,
                              String label, int col,
                              XSSFCellStyle valueStyle, XSSFCellStyle borderStyle) {
        Row firstRow = getOrCreateRow(sheet, startRow);
        Cell firstCell = firstRow.createCell(col);
        firstCell.setCellValue(label);
        firstCell.setCellStyle(valueStyle);

        for (int i = 1; i < periodCount; i++) {
            Row r = getOrCreateRow(sheet, startRow + i);
            Cell c = r.createCell(col);
            c.setCellStyle(borderStyle);
        }

        if (periodCount > 1)
            sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + periodCount - 1, col, col));
    }

    private void writeSepRow(Row row, XSSFCellStyle borderStyle, XSSFCellStyle timeStyle, boolean includeAll) {
        setCell(row, SEC2F_COL, "", borderStyle);
        setCell(row, SEC2F_COL + 1, "시간", timeStyle);
        for (int i = 0; i < FLOOR2_ROOMS.length; i++)
            setCell(row, SEC2F_COL + 2 + i, "", borderStyle);

        if (includeAll) {
            setCell(row, SEC3F_COL, "", borderStyle);
            setCell(row, SEC3F_COL + 1, "시간", timeStyle);
            for (int i = 0; i < FLOOR3_ROOMS.length; i++)
                setCell(row, SEC3F_COL + 2 + i, "", borderStyle);

            setCell(row, SEC4F_COL, "", borderStyle);
            setCell(row, SEC4F_COL + 1, "시간", timeStyle);
            for (int i = 0; i < FLOOR4_ROOMS.length; i++)
                setCell(row, SEC4F_COL + 2 + i, "", borderStyle);
        }
    }

    private void writeRoomCells(Row row, DayOfWeek day, PeriodType periodType, int periodIdx,
                                int[] rooms, int colOffset,
                                Map<String, Schedule> scheduleMap,
                                XSSFCellStyle filledStyle, XSSFCellStyle emptyStyle) {
        for (int r = 0; r < rooms.length; r++) {
            int col = colOffset + r;
            String key = makeKey(rooms[r], day, periodType, periodIdx + 1);
            Schedule s = scheduleMap.get(key);
            Cell cell = row.createCell(col);
            if (s != null) {
                String value = s.getCourseName()
                        + "\n" + (s.getProfessor() != null ? s.getProfessor() : "")
                        + "\n" + (s.getGrade() != null ? s.getGrade() : "")
                        + "-" + (s.getSection() != null ? s.getSection() : "");
                cell.setCellValue(value);
                cell.setCellStyle(filledStyle);
            } else {
                cell.setCellStyle(emptyStyle);
            }
        }
    }

    private void writeHeaders(Row row, XSSFCellStyle style) {
        setCell(row, SEC2F_COL, "요일", style);
        setCell(row, SEC2F_COL + 1, "시간", style);
        for (int i = 0; i < FLOOR2_ROOMS.length; i++)
            setCell(row, SEC2F_COL + 2 + i, "배 " + FLOOR2_ROOMS[i], style);

        setCell(row, SEC3F_COL, "요일", style);
        setCell(row, SEC3F_COL + 1, "시간", style);
        for (int i = 0; i < FLOOR3_ROOMS.length; i++)
            setCell(row, SEC3F_COL + 2 + i, "배 " + FLOOR3_ROOMS[i], style);

        setCell(row, SEC4F_COL, "요일", style);
        setCell(row, SEC4F_COL + 1, "시간", style);
        for (int i = 0; i < FLOOR4_ROOMS.length; i++)
            setCell(row, SEC4F_COL + 2 + i, "배 " + FLOOR4_ROOMS[i], style);
    }

    private void writeNightHeaders(Row row, XSSFCellStyle style) {
        setCell(row, SEC2F_COL, "요일", style);
        setCell(row, SEC2F_COL + 1, "시간", style);
        for (int i = 0; i < FLOOR2_ROOMS.length; i++)
            setCell(row, SEC2F_COL + 2 + i, "배 " + FLOOR2_ROOMS[i], style);
    }

    private void mergePeriods(Sheet sheet, Map<String, Schedule> scheduleMap,
                              DayOfWeek day, PeriodType periodType,
                              int dayRowStart, int[] rooms, int colOffset, int maxPeriod) {
        for (int r = 0; r < rooms.length; r++) {
            int col = colOffset + r;
            int p = 0;
            while (p < maxPeriod) {
                String key = makeKey(rooms[r], day, periodType, p + 1);
                Schedule s = scheduleMap.get(key);
                if (s != null && s.getPeriodEnd() > s.getPeriodStart()) {
                    int span = s.getPeriodEnd() - s.getPeriodStart();
                    int mergeStart = dayRowStart + p;
                    int mergeEnd = mergeStart + span;
                    if (mergeEnd < dayRowStart + maxPeriod) {
                        try {
                            sheet.addMergedRegion(new CellRangeAddress(mergeStart, mergeEnd, col, col));
                        } catch (Exception ignored) {}
                    }
                    p += span + 1;
                } else {
                    p++;
                }
            }
        }
    }

    private void setColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(SEC2F_COL, 5 * 256);
        sheet.setColumnWidth(SEC2F_COL + 1, 8 * 256);
        for (int i = 0; i < FLOOR2_ROOMS.length; i++)
            sheet.setColumnWidth(SEC2F_COL + 2 + i, (int)(8.17 * 256));
        sheet.setColumnWidth(SEC2F_COL + 2 + FLOOR2_ROOMS.length, 2 * 256);

        sheet.setColumnWidth(SEC3F_COL, 5 * 256);
        sheet.setColumnWidth(SEC3F_COL + 1, 8 * 256);
        for (int i = 0; i < FLOOR3_ROOMS.length; i++)
            sheet.setColumnWidth(SEC3F_COL + 2 + i, (int)(8.17 * 256));
        sheet.setColumnWidth(SEC3F_COL + 2 + FLOOR3_ROOMS.length, 2 * 256);

        sheet.setColumnWidth(SEC4F_COL, 5 * 256);
        sheet.setColumnWidth(SEC4F_COL + 1, 8 * 256);
        for (int i = 0; i < FLOOR4_ROOMS.length; i++)
            sheet.setColumnWidth(SEC4F_COL + 2 + i, (int)(8.17 * 256));
    }

    private void setCell(Row row, int col, String value, XSSFCellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private Row getOrCreateRow(Sheet sheet, int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        return row != null ? row : sheet.createRow(rowIdx);
    }

    private String makeKey(int roomNumber, DayOfWeek day, PeriodType periodType, int period) {
        return roomNumber + "_" + day.name() + "_" + periodType.name() + "_" + period;
    }

    private XSSFCellStyle makeHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕"); font.setFontHeightInPoints((short) 9); font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorder(style);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private XSSFCellStyle makeDayStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕"); font.setFontHeightInPoints((short) 10); font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private XSSFCellStyle makeBorderOnlyStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private XSSFCellStyle makeTimeStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕"); font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorder(style);
        return style;
    }

    private XSSFCellStyle makeDataStyle(XSSFWorkbook wb, boolean filled) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕"); font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorder(style);
        if (filled) {
            style.setFillForegroundColor(new XSSFColor(DATA_COLOR, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private XSSFCellStyle makeNightLabelStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕"); font.setFontHeightInPoints((short) 11); font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        style.setFillForegroundColor(new XSSFColor(NIGHT_LABEL_COLOR, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}