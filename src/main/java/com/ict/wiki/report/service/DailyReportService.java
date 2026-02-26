package com.ict.wiki.report.service;

import com.ict.wiki.inquiry.domain.Inquiry;
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
import java.util.List;
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
        palette.setColorAtIndex((short) 42, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF); // 흰색

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

        int topCount    = Math.max(allSection.size(), 10);
        int bottomCount = Math.max(inProgressSection.size(), 3);

        int rowNum = 0;

        // ===== 행0: 제목 =====
        Row r0 = createRow(sheet, rowNum++, (short) 560);
        setCell(r0, 0, "장애 발생 처리 내역", styleTitle);
        setCell(r0, 1, "", styleTitle);
        setCell(r0, 2, "", styleTitle);
        setCell(r0, 3, "", styleTitle);
        setCell(r0, 4, "", styleTitle);
        setCell(r0, 5, "", styleTitle);
        setCell(r0, 6, "", styleTitle);
        setCell(r0, 7, "", styleTitle);
        setCell(r0, 8, "", styleTitle);
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
        for (int i = 0; i < topCount; i++) {
            Row dr = createRow(sheet, rowNum++, (short) 435);
            if (i < allSection.size()) {
                Inquiry inq = allSection.get(i);
                setCell(dr, 0, inq.getBuilding() != null ? inq.getBuilding().getDisplayName() : "", styleDataC);
                setCell(dr, 1, inq.getRoomNumber() != null ? inq.getRoomNumber() + "호" : "", styleDataC);
                setCell(dr, 2, inq.getCreatedAt() != null ? inq.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "", styleDataC);
                setCell(dr, 3, inq.getDescription(), styleDataL);
                setCell(dr, 4, inq.getSolution() != null ? inq.getSolution() : "", styleDataL);
                setCell(dr, 5, "", styleDataL);
                setCell(dr, 6, "", styleDataL);
                merge(sheet, rowNum - 1, rowNum - 1, 4, 6);
                setCell(dr, 7, inq.getCompletedAt() != null ? inq.getCompletedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "", styleDataC);
                setCell(dr, 8, inq.getStatus().getDescription(), styleDataC);
            } else {
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
        }

        // ===== 민원 마지막 아래 구분행 (A~I 병합) =====
        Row rDiv = createRow(sheet, rowNum++, (short) 495);
        setCell(rDiv, 0, "", styleEmpty);
        setCell(rDiv, 1, "", styleEmpty);
        setCell(rDiv, 2, "", styleEmpty);
        setCell(rDiv, 3, "", styleEmpty);
        setCell(rDiv, 4, "", styleEmpty);
        setCell(rDiv, 5, "", styleEmpty);
        setCell(rDiv, 6, "", styleEmpty);
        setCell(rDiv, 7, "", styleEmpty);
        setCell(rDiv, 8, "", styleEmpty);
        merge(sheet, rowNum - 1, rowNum - 1, 0, 8);

        // ===== 진행중 제목 =====
        Row rSecTitle = createRow(sheet, rowNum++, (short) 495);
        setCell(rSecTitle, 0, "진행중인 장애발생 처리내역", styleSecTitle);
        setCell(rSecTitle, 1, "", styleSecTitle);
        setCell(rSecTitle, 2, "", styleSecTitle);
        setCell(rSecTitle, 3, "", styleSecTitle);
        setCell(rSecTitle, 4, "", styleSecTitle);
        setCell(rSecTitle, 5, "", styleSecTitle);
        setCell(rSecTitle, 6, "", styleSecTitle);
        setCell(rSecTitle, 7, "", styleSecTitle);
        setCell(rSecTitle, 8, "", styleSecTitle);
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
        for (int i = 0; i < bottomCount; i++) {
            Row dr = createRow(sheet, rowNum++, (short) 495);
            if (i < inProgressSection.size()) {
                Inquiry inq = inProgressSection.get(i);
                setCell(dr, 0, inq.getBuilding() != null ? inq.getBuilding().getDisplayName() : "", styleDataC);
                setCell(dr, 1, inq.getRoomNumber() != null ? inq.getRoomNumber() + "호" : "", styleDataC);
                setCell(dr, 2, inq.getCreatedAt() != null ? inq.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "", styleDataC);
                setCell(dr, 3, inq.getDescription(), styleDataL);
                setCell(dr, 4, inq.getSolution() != null ? inq.getSolution() : "", styleDataL);
                setCell(dr, 5, "", styleDataL);
                setCell(dr, 6, "", styleDataL);
                merge(sheet, rowNum - 1, rowNum - 1, 4, 6);
                setCell(dr, 7, "", styleDataC);
                setCell(dr, 8, inq.getStatus().getDescription(), styleDataC);
            } else {
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
        }

        // ===== 진행중 마지막 아래 흰색 구분행 =====
        Row rWhiteDiv = createRow(sheet, rowNum++, (short) 495);
        setCell(rWhiteDiv, 0, "", styleWhite);
        setCell(rWhiteDiv, 1, "", styleWhite);
        setCell(rWhiteDiv, 2, "", styleWhite);
        setCell(rWhiteDiv, 3, "", styleWhite);
        setCell(rWhiteDiv, 4, "", styleWhite);
        setCell(rWhiteDiv, 5, "", styleWhite);
        setCell(rWhiteDiv, 6, "", styleWhite);
        setCell(rWhiteDiv, 7, "", styleWhite);
        setCell(rWhiteDiv, 8, "", styleWhite);
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