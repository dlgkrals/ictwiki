package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {

    /**
     * 공식 명칭으로 조회
     */
    Optional<Software> findByName(String name);

    /**
     * 기본 소프트웨어 목록 조회
     */
    @Query("SELECT s FROM Software s WHERE s.isDefault = true ORDER BY s.name ASC")
    List<Software> findAllDefault();

    /**
     * 특수 소프트웨어 목록 조회 (비기본)
     */
    @Query("SELECT s FROM Software s WHERE s.isDefault = false ORDER BY s.name ASC")
    List<Software> findAllSpecial();

    /**
     * 전체 목록 (이름 순)
     */
    @Query("SELECT s FROM Software s ORDER BY s.isDefault DESC, s.name ASC")
    List<Software> findAllOrdered();

    /**
     * alias JSON에서 검색
     * - 소프트웨어명 normalize 후 alias 포함 여부 확인
     * - 예: "스케치업", "sketchup", "sketch up" 모두 매칭
     */
    @Query("SELECT s FROM Software s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.aliases) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Software> searchByNameOrAlias(@Param("keyword") String keyword);

    /**
     * 특정 강의실에 설치된 소프트웨어 목록
     */
    @Query("""
            SELECT s FROM Software s
            JOIN ClassroomSoftware cs ON cs.software.id = s.id
            WHERE cs.classroom.id = :classroomId
            ORDER BY s.isDefault DESC, s.name ASC
            """)
    List<Software> findByClassroomId(@Param("classroomId") Long classroomId);
}