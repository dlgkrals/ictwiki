package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.ClassroomSoftware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomSoftwareRepository extends JpaRepository<ClassroomSoftware, Long> {

    /**
     * 특정 강의실의 전체 설치 목록
     */
    @Query("SELECT cs FROM ClassroomSoftware cs JOIN FETCH cs.software WHERE cs.classroom.id = :classroomId")
    List<ClassroomSoftware> findByClassroomId(@Param("classroomId") Long classroomId);

    @Query("SELECT cs FROM ClassroomSoftware cs JOIN FETCH cs.classroom JOIN FETCH cs.software ORDER BY cs.classroom.roomNumber")
    List<ClassroomSoftware> findAllWithClassroomAndSoftware();

    /**
     * 특정 강의실 + 특정 소프트웨어 매핑 존재 여부
     */
    Optional<ClassroomSoftware> findByClassroomIdAndSoftwareId(Long classroomId, Long softwareId);

    /**
     * 특정 강의실의 소프트웨어 전체 삭제 (초기화 후 재등록 시)
     */
    @Modifying
    @Query("DELETE FROM ClassroomSoftware cs WHERE cs.classroom.id = :classroomId")
    void deleteByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * 특정 강의실에서 특정 소프트웨어 삭제
     */
    @Modifying
    @Query("DELETE FROM ClassroomSoftware cs WHERE cs.classroom.id = :classroomId AND cs.software.id = :softwareId")
    void deleteByClassroomIdAndSoftwareId(
            @Param("classroomId") Long classroomId,
            @Param("softwareId") Long softwareId
    );
}