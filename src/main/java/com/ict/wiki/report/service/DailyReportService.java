package com.ict.wiki.report.service;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryLocation;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import com.ict.wiki.login.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final InquiryRepository inquiryRepository;

    public byte[] generateReport(User user, LocalDate date) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();

        var palette = workbook.getCustomPalette();
        palette.setColorAtIndex((short) 40, (byte) 0xEE, (byte) 0xEC, (byte) 0xE1);
        palette.setColorAtIndex((short) 41, (byte) 0xC0, (byte) 0xC0, (byte) 0xC0);
        palette.setColorAtIndex((short) 42, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);

        CellStyle styleTitle     = makeStyle(workbook, false, (short) 440, (short) -1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false);
        CellStyle styleDateBold  = makeStyle(workbook, true,  (short) 280, (short) -1, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, false);
        CellStyle styleNameNorm  = makeStyle(workbook, false, (short) 220, (short) -1, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, false);
        CellStyle styleEmpty     = makeStyle(workbook, false, (short) 200, (short) -1, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, false);
        CellStyle styleWhite     = makeStyle(workbook, false, (short) 200, (short) 42, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, false);
        CellStyle styleHeader    = makeStyle(workbook, true,  (short) 220, (short) 40, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true);
        CellStyle styleDataC     = makeStyle(workbook, false, (short) 200, (short) -1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true);
        CellStyle styleDataL     = makeStyle(workbook, false, (short) 200, (short) -1, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, true);
        CellStyle styleSecTitle  = makeStyle(workbook, true,  (short) 360, (short) -1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false);
        CellStyle styleSignLabel = makeStyle(workbook, true,  (short) 220, (short) 41, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true);
        CellStyle styleSignValue = makeStyle(workbook, false, (short) 220, (short) -1, HorizontalAlignment.LEFT,   VerticalAlignment.CENTER, true);

        String sheetName = date.getMonthValue() + "월" + date.getDayOfMonth() + "일";
        Sheet sheet = workbook.createSheet(sheetName);

        sheet.setColumnWidth(0, 2133);
        sheet.setColumnWidth(1, 2730);
        sheet.setColumnWidth(2, 2432);
        sheet.setColumnWidth(3, 14506);
        sheet.setColumnWidth(4, 2730);
        sheet.setColumnWidth(5, 2730);
        sheet.setColumnWidth(6, 7680);
        sheet.setColumnWidth(7, 2602);
        sheet.setColumnWidth(8, 2602);

        List<Inquiry> allInquiries = inquiryRepository.findByWorkerOrSubWorkerAndDate(user.getId(), date);
        List<Inquiry> allSection = allInquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.COMPLETED
                        || i.getStatus() == InquiryStatus.IN_PROGRESS)
                .collect(Collectors.toList());
        List<Inquiry> inProgressSection = allInquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        // 민원별 위치 행 수 합산
        int filledTopRows    = allSection.stream().mapToInt(i -> formatLocationLines(i).size()).sum();
        int filledBottomRows = inProgressSection.stream().mapToInt(i -> formatLocationLines(i).size()).sum();
        int topCount    = Math.max(filledTopRows, 10);
        int bottomCount = Math.max(filledBottomRows, 3);

        int rowNum = 0;

        // ===== 행0: 제목 =====
        Row r0 = createRow(sheet, rowNum++, (short) 560);
        setCell(r0, 0, "장애 발생 처리 내역", styleTitle);
        for (int c = 1; c <= 8; c++) setCell(r0, c, "", styleTitle);
        merge(sheet, 0, 0, 0, 8);

        // ===== 행1: 빈행 =====
        createRow(sheet, rowNum++, (short) 135);

        // ===== 행2: 날짜 / 담당자 =====
        Row r2 = createRow(sheet, rowNum++, (short) 375);
        setCell(r2, 0, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), styleDateBold);
        setCell(r2, 1, "", styleDateBold);
        setCell(r2, 2, "", styleDateBold);
        merge(sheet, 2, 2, 0, 2);
        setCell(r2, 3, user.getName() + " " + user.getRole().getDisplayName(), styleNameNorm);
        setCell(r2, 4, "", styleEmpty);
        setCell(r2, 5, "", styleEmpty);
        setCell(r2, 6, "", styleEmpty);
        setCell(r2, 7, "", styleEmpty);
        setCell(r2, 8, "", styleEmpty);
        merge(sheet, 2, 2, 4, 8);

        // ===== 행3: 빈행 =====
        createRow(sheet, rowNum++, (short) 120);

        // ===== 행4: 헤더 =====
        Row r4 = createRow(sheet, rowNum++, (short) 435);
        setCell(r4, 0, "장소", styleHeader);
        setCell(r4, 1, "", styleHeader);
        merge(sheet, 4, 4, 0, 1);
        setCell(r4, 2, "접수시간", styleHeader);
        setCell(r4, 3, "증상", styleHeader);
        setCell(r4, 4, "처리내역", styleHeader);
        setCell(r4, 5, "", styleHeader);
        setCell(r4, 6, "", styleHeader);
        merge(sheet, 4, 4, 4, 6);
        setCell(r4, 7, "처리시간", styleHeader);
        setCell(r4, 8, "완료여부", styleHeader);

        // ===== 위쪽 데이터 섹션 =====
        for (Inquiry inq : allSection) {
            rowNum += writeInquiryRows(sheet, rowNum, inq, false,
                    styleDataC, styleDataC, styleDataL, styleDataL, styleDataC);
        }
        // 남은 빈 행 채우기
        for (int i = filledTopRows; i < topCount; i++) {
            Row dr = createRow(sheet, rowNum++, (short) 435);
            setCell(dr, 0, "", styleDataC);
            setCell(dr, 1, "", styleDataC);
            setCell(dr, 2, "", styleDataC);
            setCell(dr, 3, "", styleDataL);
            setCell(dr, 4, "", styleDataL);
            setCell(dr, 5, "", styleDataL);
            setCell(dr, 6, "", styleDataL);
            merge(sheet, rowNum - 1, rowNum - 1, 4, 6);
            setCell(dr, 7, "", styleDataC);
            setCell(dr, 8, "", styleDataC);
        }

        // ===== 민원 마지막 아래 구분행 =====
        Row rDiv = createRow(sheet, rowNum++, (short) 495);
        for (int c = 0; c <= 8; c++) setCell(rDiv, c, "", styleEmpty);
        merge(sheet, rowNum - 1, rowNum - 1, 0, 8);

        // ===== 진행중 제목 =====
        Row rSecTitle = createRow(sheet, rowNum++, (short) 495);
        setCell(rSecTitle, 0, "진행중인 장애발생 처리내역", styleSecTitle);
        for (int c = 1; c <= 8; c++) setCell(rSecTitle, c, "", styleSecTitle);
        merge(sheet, rowNum - 1, rowNum - 1, 0, 8);

        // ===== 진행중 헤더 =====
        Row rBotHeader = createRow(sheet, rowNum++, (short) 495);
        setCell(rBotHeader, 0, "위치", styleHeader);
        setCell(rBotHeader, 1, "", styleHeader);
        merge(sheet, rowNum - 1, rowNum - 1, 0, 1);
        setCell(rBotHeader, 2, "접수일자", styleHeader);
        setCell(rBotHeader, 3, "증상", styleHeader);
        setCell(rBotHeader, 4, "처리내역", styleHeader);
        setCell(rBotHeader, 5, "", styleHeader);
        setCell(rBotHeader, 6, "", styleHeader);
        merge(sheet, rowNum - 1, rowNum - 1, 4, 6);
        setCell(rBotHeader, 7, "처리일자", styleHeader);
        setCell(rBotHeader, 8, "완료여부", styleHeader);

        // ===== 진행중 데이터 섹션 =====
        for (Inquiry inq : inProgressSection) {
            rowNum += writeInquiryRows(sheet, rowNum, inq, true,
                    styleDataC, styleDataC, styleDataL, styleDataL, styleDataC);
        }
        // 남은 빈 행 채우기
        for (int i = filledBottomRows; i < bottomCount; i++) {
            Row dr = createRow(sheet, rowNum++, (short) 495);
            setCell(dr, 0, "", styleDataC);
            setCell(dr, 1, "", styleDataC);
            setCell(dr, 2, "", styleDataC);
            setCell(dr, 3, "", styleDataL);
            setCell(dr, 4, "", styleDataL);
            setCell(dr, 5, "", styleDataL);
            setCell(dr, 6, "", styleDataL);
            merge(sheet, rowNum - 1, rowNum - 1, 4, 6);
            setCell(dr, 7, "", styleDataC);
            setCell(dr, 8, "", styleDataC);
        }

        // ===== 진행중 마지막 아래 흰색 구분행 =====
        Row rWhiteDiv = createRow(sheet, rowNum++, (short) 495);
        for (int c = 0; c <= 8; c++) setCell(rWhiteDiv, c, "", styleWhite);
        merge(sheet, rowNum - 1, rowNum - 1, 0, 8);

        // ===== 서명란 =====
        Row s0 = createRow(sheet, rowNum, (short) 495);
        Row s1 = createRow(sheet, rowNum + 1, (short) 495);

        setCell(s0, 0, "처리자", styleSignLabel);
        setCell(s0, 1, "", styleSignLabel);
        setCell(s1, 0, "", styleSignLabel);
        setCell(s1, 1, "", styleSignLabel);
        merge(sheet, rowNum, rowNum + 1, 0, 1);

        setCell(s0, 2, "회사", styleSignLabel);
        setCell(s0, 3, "㈜제이엠티미디어", styleSignValue);

        setCell(s0, 4, "확인자", styleSignLabel);
        setCell(s1, 4, "", styleSignLabel);
        merge(sheet, rowNum, rowNum + 1, 4, 4);

        setCell(s0, 5, "부서", styleSignLabel);
        setCell(s0, 6, "", styleSignValue);
        setCell(s0, 7, "", styleSignValue);
        setCell(s0, 8, "", styleSignValue);
        merge(sheet, rowNum, rowNum, 6, 8);

        setCell(s1, 2, "성명", styleSignLabel);
        setCell(s1, 3, user.getName() + " " + user.getRole().getDisplayName(), styleSignValue);
        setCell(s1, 5, "성명", styleSignLabel);
        setCell(s1, 6, "", styleSignValue);
        setCell(s1, 7, "", styleSignValue);
        setCell(s1, 8, "", styleSignValue);
        merge(sheet, rowNum + 1, rowNum + 1, 6, 8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    // ========== 내부 record ==========

    private record LocationLine(String building, String room) {}

    // ========== 헬퍼: 위치 라인 목록 생성 ==========

    /**
     * 민원의 위치 목록을 건물별로 그룹핑하여 건물명/호실 쌍 리스트로 반환
     * 예: [("배양관", "304, 309호"), ("누리관", "603호")]
     */
    private List<LocationLine> formatLocationLines(Inquiry inq) {
        if (inq.getLocations() == null || inq.getLocations().isEmpty()) {
            return List.of(new LocationLine("", ""));
        }

        // 건물별 그룹핑 (입력 순서 유지)
        Map<String, List<InquiryLocation>> grouped = new LinkedHashMap<>();
        for (InquiryLocation loc : inq.getLocations()) {
            grouped.computeIfAbsent(loc.getBuilding().getDisplayName(), k -> new ArrayList<>()).add(loc);
        }

        List<LocationLine> lines = new ArrayList<>();
        for (Map.Entry<String, List<InquiryLocation>> entry : grouped.entrySet()) {
            String buildingName = entry.getKey();
            List<InquiryLocation> locs = entry.getValue();

            // 숫자 호실과 특수 호실 분리
            List<Integer> numbers = new ArrayList<>();
            List<String> specials = new ArrayList<>();

            for (InquiryLocation loc : locs) {
                if (loc.getRoomNumber() != null && loc.getRoomName() == null) {
                    numbers.add(loc.getRoomNumber());
                } else {
                    specials.add(loc.getFormatted().replace(buildingName, "").trim());
                }
            }

            List<String> parts = new ArrayList<>();
            if (!numbers.isEmpty()) parts.add(compressRoomNumbers(numbers));
            parts.addAll(specials);

            lines.add(new LocationLine(buildingName, String.join(", ", parts)));
        }
        return lines;
    }

    /**
     * 숫자 호실 목록을 연속 구간으로 압축
     * 예: [304, 306, 307, 308] → "304, 306~308호"
     */
    private String compressRoomNumbers(List<Integer> numbers) {
        Collections.sort(numbers);

        List<String> segments = new ArrayList<>();
        int start = numbers.get(0);
        int end = start;

        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) == end + 1) {
                end = numbers.get(i);
            } else {
                segments.add(start == end ? String.valueOf(start) : start + "~" + end);
                start = numbers.get(i);
                end = start;
            }
        }
        segments.add(start == end ? String.valueOf(start) : start + "~" + end);

        // 마지막에만 "호" 붙이기
        return String.join(", ", segments) + "호";
    }

    /**
     * 민원 하나를 위치 수만큼 행으로 렌더링
     * 위치가 여러 개면 나머지 셀(접수시간, 증상, 처리내역, 처리시간, 완료여부)을 세로 병합
     * 장소는 건물명(col 0) / 호실(col 1) 두 칸으로 분리
     *
     * @return 소비한 행 수
     */
    private int writeInquiryRows(Sheet sheet, int rowNum, Inquiry inq, boolean isBottom,
                                 CellStyle locationStyle, CellStyle timeStyle,
                                 CellStyle descStyle, CellStyle solutionStyle,
                                 CellStyle statusStyle) {
        List<LocationLine> lines = formatLocationLines(inq);
        int rowCount = lines.size();
        short rowHeight = isBottom ? (short) 495 : (short) 435;

        for (int i = 0; i < rowCount; i++) {
            Row dr = createRow(sheet, rowNum + i, rowHeight);

            // 건물명 / 호실 분리 (두 칸)
            setCell(dr, 0, lines.get(i).building(), locationStyle);
            setCell(dr, 1, lines.get(i).room(), locationStyle);

            if (i == 0) {
                // 첫 번째 행에만 값 입력
                setCell(dr, 2, inq.getCreatedAt() != null
                        ? inq.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "", timeStyle);
                setCell(dr, 3, inq.getDescription(), descStyle);
                setCell(dr, 4, inq.getSolution() != null ? inq.getSolution() : "", solutionStyle);
                setCell(dr, 5, "", solutionStyle);
                setCell(dr, 6, "", solutionStyle);
                setCell(dr, 7, !isBottom && inq.getCompletedAt() != null
                        ? inq.getCompletedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "", timeStyle);
                setCell(dr, 8, inq.getStatus().getDescription(), statusStyle);
            } else {
                // 나머지 행은 빈 셀
                setCell(dr, 2, "", timeStyle);
                setCell(dr, 3, "", descStyle);
                setCell(dr, 4, "", solutionStyle);
                setCell(dr, 5, "", solutionStyle);
                setCell(dr, 6, "", solutionStyle);
                setCell(dr, 7, "", timeStyle);
                setCell(dr, 8, "", statusStyle);
            }
        }

        // 처리내역(4~6) 병합: 단일 행이면 가로 병합만, 여러 행이면 세로+가로 병합
        if (rowCount == 1) {
            merge(sheet, rowNum, rowNum, 4, 6);
        } else {
            merge(sheet, rowNum, rowNum + rowCount - 1, 2, 2); // 접수시간
            merge(sheet, rowNum, rowNum + rowCount - 1, 3, 3); // 증상
            merge(sheet, rowNum, rowNum + rowCount - 1, 4, 6); // 처리내역
            merge(sheet, rowNum, rowNum + rowCount - 1, 7, 7); // 처리시간
            merge(sheet, rowNum, rowNum + rowCount - 1, 8, 8); // 완료여부
        }

        return rowCount;
    }

    // ========== 공통 유틸 ==========

    private CellStyle makeStyle(HSSFWorkbook wb, boolean bold, short fontHeight,
                                short bgColorIndex, HorizontalAlignment halign,
                                VerticalAlignment valign, boolean borders) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("맑은 고딕");
        font.setBold(bold);
        font.setFontHeight(fontHeight);
        style.setFont(font);
        style.setAlignment(halign);
        style.setVerticalAlignment(valign);
        if (bgColorIndex >= 0) {
            style.setFillForegroundColor(bgColorIndex);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (borders) {
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
        }
        return style;
    }

    private Row createRow(Sheet sheet, int rowNum, short height) {
        Row row = sheet.createRow(rowNum);
        row.setHeight(height);
        return row;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void merge(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }
}