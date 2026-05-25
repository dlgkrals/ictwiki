package com.ict.wiki.report.service;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.inquiry.domain.InquiryLocation;
import com.ict.wiki.inquiry.domain.InquiryStatus;
import com.ict.wiki.inquiry.repository.InquiryRepository;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.report.dto.response.DailyReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final InquiryRepository inquiryRepository;

    public DailyReportResponse getReportData(User user, LocalDate date) {

        List<Inquiry> allInquiries = inquiryRepository.findByWorkerOrSubWorkerAndDate(user.getId(), date);

        List<Inquiry> allSection = allInquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.COMPLETED
                        || i.getStatus() == InquiryStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        List<Inquiry> inProgressSection = allInquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        int filledTopRows    = allSection.stream().mapToInt(i -> formatLocationLines(i).size()).sum();
        int filledBottomRows = inProgressSection.stream().mapToInt(i -> formatLocationLines(i).size()).sum();

        return DailyReportResponse.builder()
                .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .userName(user.getName())
                .userRole(user.getRole().getDisplayName())
                .sheetName(date.getMonthValue() + "월" + date.getDayOfMonth() + "일")
                .allSection(allSection.stream().map(inq -> toRowData(inq, false)).collect(Collectors.toList()))
                .topCount(Math.max(filledTopRows, 10))
                .inProgressSection(inProgressSection.stream().map(inq -> toRowData(inq, true)).collect(Collectors.toList()))
                .bottomCount(Math.max(filledBottomRows, 3))
                .build();
    }

    private DailyReportResponse.InquiryRowData toRowData(Inquiry inq, boolean isBottom) {
        return DailyReportResponse.InquiryRowData.builder()
                .locations(formatLocationLines(inq).stream()
                        .map(l -> DailyReportResponse.LocationLine.builder()
                                .building(l.building())
                                .room(l.room())
                                .build())
                        .collect(Collectors.toList()))
                .receivedTime(inq.getCreatedAt() != null
                        ? inq.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                .description(inq.getDescription())
                .solution(inq.getSolution() != null ? inq.getSolution() : "")
                .completedTime(!isBottom && inq.getCompletedAt() != null
                        ? inq.getCompletedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                .status(inq.getStatus().getDescription())
                .build();
    }

    // ========== 내부 record ==========

    private record LocationLine(String building, String room) {}

    // ========== 헬퍼: 위치 라인 목록 생성 ==========

    private List<LocationLine> formatLocationLines(Inquiry inq) {
        if (inq.getLocations() == null || inq.getLocations().isEmpty()) {
            return List.of(new LocationLine("", ""));
        }

        Map<String, List<InquiryLocation>> grouped = new LinkedHashMap<>();
        for (InquiryLocation loc : inq.getLocations()) {
            grouped.computeIfAbsent(loc.getBuilding().getDisplayName(), k -> new ArrayList<>()).add(loc);
        }

        List<LocationLine> lines = new ArrayList<>();
        for (Map.Entry<String, List<InquiryLocation>> entry : grouped.entrySet()) {
            String buildingName = entry.getKey();
            List<InquiryLocation> locs = entry.getValue();

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
        return String.join(", ", segments) + "호";
    }
}