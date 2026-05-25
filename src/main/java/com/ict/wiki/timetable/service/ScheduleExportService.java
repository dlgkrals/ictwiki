package com.ict.wiki.timetable.service;

import com.ict.wiki.timetable.domain.DayOfWeek;
import com.ict.wiki.timetable.domain.PeriodType;
import com.ict.wiki.timetable.domain.Schedule;
import com.ict.wiki.timetable.dto.response.ScheduleExportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final int[][] NIGHT_GROUPS = {{0, 1, 2}, {3, 4, 5}};

    public ScheduleExportResponse getExportData(String semester) {
        List<Schedule> schedules = scheduleService.getBySemester(semester);

        Map<String, Schedule> scheduleMap = new LinkedHashMap<>();
        for (Schedule s : schedules) {
            if (s.getClassroom() == null) continue;
            scheduleMap.put(
                    makeKey(s.getClassroom().getRoomNumber(), s.getDayOfWeek(), s.getPeriodType(), s.getPeriodStart()),
                    s
            );
        }

        List<ScheduleExportResponse.FloorData> floors = new ArrayList<>();
        floors.add(buildFloorData("2F공용PC실습실", FLOOR2_ROOMS, scheduleMap));
        floors.add(buildFloorData("3F공용PC실습실", FLOOR3_ROOMS, scheduleMap));
        floors.add(buildFloorData("4F공용PC실습실", FLOOR4_ROOMS, scheduleMap));

        return ScheduleExportResponse.builder()
                .semester(semester)
                .floors(floors)
                .build();
    }

    private ScheduleExportResponse.FloorData buildFloorData(
            String name, int[] rooms, Map<String, Schedule> scheduleMap) {

        List<Integer> roomList = new ArrayList<>();
        for (int r : rooms) roomList.add(r);

        List<ScheduleExportResponse.DayData> days = new ArrayList<>();
        for (DayOfWeek day : DAYS) {
            days.add(buildDayData(day, rooms, scheduleMap));
        }

        return ScheduleExportResponse.FloorData.builder()
                .name(name)
                .rooms(roomList)
                .days(days)
                .build();
    }

    private ScheduleExportResponse.DayData buildDayData(
            DayOfWeek day, int[] rooms, Map<String, Schedule> scheduleMap) {

        // 주간 교시별
        List<ScheduleExportResponse.PeriodRow> dayPeriods = new ArrayList<>();
        for (int p = 0; p < DAY_PERIODS.length; p++) {
            Map<Integer, ScheduleExportResponse.CellData> cells = new LinkedHashMap<>();
            for (int room : rooms) {
                String key = makeKey(room, day, PeriodType.DAY, p + 1);
                Schedule s = scheduleMap.get(key);
                cells.put(room, s != null ? toCellData(s) : null);
            }
            dayPeriods.add(ScheduleExportResponse.PeriodRow.builder()
                    .periodLabel(DAY_PERIODS[p])
                    .cells(cells)
                    .build());
        }

        // 야간 그룹별
        List<ScheduleExportResponse.NightGroup> nightGroups = new ArrayList<>();
        for (int[] group : NIGHT_GROUPS) {
            List<String> labels = new ArrayList<>();
            for (int idx : group) labels.add(NIGHT_PERIODS[idx]);

            // 야간은 그룹 첫 번째 교시 기준으로 셀 데이터 조회
            Map<Integer, ScheduleExportResponse.CellData> cells = new LinkedHashMap<>();
            for (int room : rooms) {
                String key = makeKey(room, day, PeriodType.NIGHT, group[0] + 1);
                Schedule s = scheduleMap.get(key);
                cells.put(room, s != null ? toCellData(s) : null);
            }

            nightGroups.add(ScheduleExportResponse.NightGroup.builder()
                    .periodLabels(labels)
                    .cells(cells)
                    .build());
        }

        return ScheduleExportResponse.DayData.builder()
                .day(day.name())
                .label(day.getLabel())
                .dayPeriods(dayPeriods)
                .nightGroups(nightGroups)
                .build();
    }

    private ScheduleExportResponse.CellData toCellData(Schedule s) {
        return ScheduleExportResponse.CellData.builder()
                .courseName(s.getCourseName())
                .professor(s.getProfessor())
                .grade(s.getGrade() != null ? String.valueOf(s.getGrade()) : "")
                .section(s.getSection() != null ? s.getSection() : "")
                .rowSpan(s.getPeriodEnd() - s.getPeriodStart() + 1)
                .build();
    }

    private String makeKey(int roomNumber, DayOfWeek day, PeriodType periodType, int period) {
        return roomNumber + "_" + day.name() + "_" + periodType.name() + "_" + period;
    }
}