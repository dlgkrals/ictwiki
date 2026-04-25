package com.ict.wiki.timetable.service;

import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import com.ict.wiki.timetable.domain.Classroom;
import com.ict.wiki.timetable.domain.ClassroomSoftware;
import com.ict.wiki.timetable.domain.Software;
import com.ict.wiki.timetable.dto.response.ClassroomSoftwareResponse;
import com.ict.wiki.timetable.dto.response.ClassroomWithSoftwaresResponse;
import com.ict.wiki.timetable.repository.ClassroomRepository;
import com.ict.wiki.timetable.repository.ClassroomSoftwareRepository;
import com.ict.wiki.timetable.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 강의실 서비스
 * - 강의실 CRUD
 * - 강의실별 소프트웨어 설치 목록 관리 (조교/관리자 전용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomSoftwareRepository classroomSoftwareRepository;
    private final SoftwareRepository softwareRepository;

    // ========== 강의실 CRUD ==========

    /**
     * 전체 강의실 목록 (층 → 호실 순)
     */
    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAllOrdered();
    }

    /**
     * 강의실 단건 조회
     */
    public Classroom findById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.CLASSROOM_NOT_FOUND));
    }

    /**
     * 호실 번호로 조회
     */
    public Classroom findByRoomNumber(Integer roomNumber) {
        return classroomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.CLASSROOM_NOT_FOUND));
    }

    /**
     * 강의실 등록
     */
    @Transactional
    public Classroom createClassroom(Integer roomNumber, Integer floor, Integer grade) {
        if (classroomRepository.findByRoomNumber(roomNumber).isPresent()) {
            throw TimetableException.of(TimetableErrorCode.CLASSROOM_DUPLICATE);
        }
        Classroom classroom = Classroom.of(roomNumber, floor, grade);
        return classroomRepository.save(classroom);
    }

    /**
     * 강의실 등급 수정
     */
    @Transactional
    public Classroom updateGrade(Long id, Integer grade) {
        Classroom classroom = findById(id);
        classroom.updateGrade(grade);
        return classroom;
    }

    // ========== 소프트웨어 설치 목록 관리 ==========

    /**
     * 강의실에 설치된 소프트웨어 목록 조회
     */
    public List<ClassroomSoftware> getSoftwares(Long classroomId) {
        findById(classroomId);
        return classroomSoftwareRepository.findByClassroomId(classroomId);
    }

    public List<ClassroomWithSoftwaresResponse> getAllClassroomsWithSoftwares() {
        List<ClassroomSoftware> allMappings = classroomSoftwareRepository.findAllWithClassroomAndSoftware();

        return allMappings.stream()
                .collect(Collectors.groupingBy(cs -> cs.getClassroom().getId(),
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    Classroom c = entry.getValue().get(0).getClassroom();
                    List<ClassroomSoftwareResponse> softwares = entry.getValue().stream()
                            .map(ClassroomSoftwareResponse::from)
                            .toList();
                    return ClassroomWithSoftwaresResponse.of(c, softwares);
                })
                .toList();
    }

    /**
     * 강의실에 소프트웨어 추가
     */
    @Transactional
    public ClassroomSoftware addSoftware(Long classroomId, Long softwareId) {
        Classroom classroom = findById(classroomId);
        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.SOFTWARE_NOT_FOUND));

        if (classroomSoftwareRepository.findByClassroomIdAndSoftwareId(classroomId, softwareId).isPresent()) {
            throw TimetableException.of(TimetableErrorCode.CLASSROOM_SOFTWARE_DUPLICATE);
        }

        ClassroomSoftware cs = ClassroomSoftware.of(classroom, software);
        log.info("강의실 소프트웨어 추가 - 강의실: {}호, SW: {}", classroom.getRoomNumber(), software.getName());
        return classroomSoftwareRepository.save(cs);
    }

    /**
     * 강의실에서 소프트웨어 삭제
     */
    @Transactional
    public void removeSoftware(Long classroomId, Long softwareId) {
        classroomSoftwareRepository.findByClassroomIdAndSoftwareId(classroomId, softwareId)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.CLASSROOM_SOFTWARE_NOT_FOUND));
        classroomSoftwareRepository.deleteByClassroomIdAndSoftwareId(classroomId, softwareId);
        log.info("강의실 소프트웨어 삭제 - classroomId: {}, softwareId: {}", classroomId, softwareId);
    }

    /**
     * 특정 소프트웨어를 모두 보유한 강의실 목록 조회
     * - 보강/시간표 배정 시 후보 강의실 추출용
     */
    public List<Classroom> findAvailableClassrooms(List<Long> softwareIds) {
        if (softwareIds == null || softwareIds.isEmpty()) {
            return classroomRepository.findAllOrdered();
        }
        return classroomRepository.findClassroomsHavingAllSoftwares(softwareIds, softwareIds.size());
    }
}