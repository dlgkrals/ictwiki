package com.ict.wiki.timetable.service;

import com.ict.wiki.timetable.domain.Classroom;
import com.ict.wiki.timetable.domain.ClassroomSoftware;
import com.ict.wiki.timetable.domain.Schedule;
import com.ict.wiki.timetable.domain.Software;
import com.ict.wiki.timetable.domain.SoftwareMatchFailure;
import com.ict.wiki.timetable.repository.ClassroomSoftwareRepository;
import com.ict.wiki.timetable.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomSoftwareAutoService {

    private final ScheduleRepository scheduleRepository;
    private final ClassroomSoftwareRepository classroomSoftwareRepository;
    private final SoftwareService softwareService;
    private final ClassroomService classroomService;
    private final SoftwareMatchFailureService failureService;

    @Transactional
    public AutoResult autoAssign(String semester) {
        List<Schedule> schedules = scheduleRepository.findBySemester(semester);

        Map<Long, Set<String>> classroomNotes = new LinkedHashMap<>();
        for (Schedule s : schedules) {
            if (s.getClassroom() == null || s.getSoftwareNote() == null || s.getSoftwareNote().isBlank()) continue;
            classroomNotes
                    .computeIfAbsent(s.getClassroom().getId(), k -> new LinkedHashSet<>())
                    .add(s.getSoftwareNote().trim());
        }

        if (classroomNotes.isEmpty()) {
            return new AutoResult(0, 0, 0);
        }

        List<Software> allSoftwares = softwareService.getAllSoftwares();

        Map<Software, List<String>> softwareKeywords = new LinkedHashMap<>();
        for (Software sw : allSoftwares) {
            List<String> keywords = new ArrayList<>();
            keywords.add(sw.getName().toLowerCase());
            for (String alias : softwareService.fromJson(sw.getAliases())) {
                keywords.add(alias.toLowerCase());
            }
            softwareKeywords.put(sw, keywords);
        }

        int assignedCount = 0;
        int classroomCount = 0;
        List<SoftwareMatchFailure> failures = new ArrayList<>();

        for (Map.Entry<Long, Set<String>> entry : classroomNotes.entrySet()) {
            Long classroomId = entry.getKey();
            Set<String> notes = entry.getValue();

            try {
                Classroom classroom = classroomService.findById(classroomId);
                classroomSoftwareRepository.deleteByClassroomId(classroomId);

                Set<Long> addedIds = new HashSet<>();

                // is_default 소프트웨어 붙박이 추가
                for (Software sw : allSoftwares) {
                    if (sw.isDefault()) {
                        classroomSoftwareRepository.save(ClassroomSoftware.of(classroom, sw));
                        addedIds.add(sw.getId());
                        assignedCount++;
                    }
                }

                for (String note : notes) {
                    // 기본세팅 노트는 스킵
                    if (note.replaceAll("\\s+", "").equals("기본세팅")) continue;

                    // \n으로 분리해서 줄별로 매칭
                    String[] lines = note.split("\\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isBlank()) continue;

                        // "1. " "2. " 같은 앞 번호 제거 (뒤에 공백 필수)
                        line = line.replaceAll("^\\d+\\.\\s+", "");
                        if (line.isBlank()) continue;

                        // 대괄호 태그 제거 [가상화 소프트웨어] 같은 앞 태그
                        line = line.replaceAll("^\\[.*?\\]\\s*", "");
                        if (line.isBlank()) continue;

                        String lineLower = line.toLowerCase();

                        // 각 소프트웨어와 매칭 체크
                        for (Map.Entry<Software, List<String>> swEntry : softwareKeywords.entrySet()) {
                            Software sw = swEntry.getKey();
                            if (addedIds.contains(sw.getId())) continue;
                            boolean matched = swEntry.getValue().stream().anyMatch(lineLower::contains);
                            if (matched) {
                                classroomSoftwareRepository.save(ClassroomSoftware.of(classroom, sw));
                                addedIds.add(sw.getId());
                                assignedCount++;
                                log.debug("매칭 - {}호 ← {}", classroom.getRoomNumber(), sw.getName());
                            }
                        }

                        final String finalLine = line;
                        boolean anyMatched = softwareKeywords.entrySet().stream()
                                .anyMatch(swEntry -> swEntry.getValue().stream().anyMatch(lineLower::contains));
                        if (!anyMatched) {
                            boolean alreadyAdded = failures.stream().anyMatch(f ->
                                    f.getRoomNumber().equals(classroom.getRoomNumber()) && f.getNote().equals(finalLine));
                            if (!alreadyAdded) {
                                failures.add(SoftwareMatchFailure.of(semester, classroom.getRoomNumber(), finalLine));
                                log.warn("매칭 실패 - {}호: {}", classroom.getRoomNumber(), finalLine);
                            }
                        }
                    }
                }

                classroomCount++;

            } catch (Exception e) {
                log.warn("강의실 처리 실패 - classroomId: {}", classroomId, e);
            }
        }

        // 학기 실패 내역 교체 저장
        failureService.replaceFailures(semester, failures);

        log.info("소프트웨어 자동 매칭 완료 - 강의실: {}개, 매핑: {}건, 실패: {}건",
                classroomCount, assignedCount, failures.size());

        return new AutoResult(classroomCount, assignedCount, failures.size());
    }

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) return year + "-1";
        return year + "-2";
    }

    public record AutoResult(
            int classroomCount,
            int assignedCount,
            int failureCount
    ) {}
}