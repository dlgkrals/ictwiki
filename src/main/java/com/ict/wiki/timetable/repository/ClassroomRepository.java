package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    /**
     * 호실 번호로 조회
     */
    Optional<Classroom> findByRoomNumber(Integer roomNumber);

    /**
     * 층별 조회 (호실 번호 순)
     */
    @Query("SELECT c FROM Classroom c WHERE c.floor = :floor ORDER BY c.roomNumber ASC")
    List<Classroom> findByFloorOrderByRoomNumber(@Param("floor") Integer floor);

    /**
     * 등급별 조회
     */
    @Query("SELECT c FROM Classroom c WHERE c.grade = :grade ORDER BY c.roomNumber ASC")
    List<Classroom> findByGradeOrderByRoomNumber(@Param("grade") Integer grade);

    /**
     * 전체 조회 (층 → 호실 순)
     */
    @Query("SELECT c FROM Classroom c ORDER BY c.floor ASC, c.roomNumber ASC")
    List<Classroom> findAllOrdered();

    /**
     * 특정 소프트웨어가 설치된 강의실 목록
     * - 보강 배정 시 소프트웨어 조건 필터링용
     */
    @Query("""
            SELECT DISTINCT c FROM Classroom c
            JOIN c.classroomSoftwares cs
            WHERE cs.software.id IN :softwareIds
            GROUP BY c
            HAVING COUNT(DISTINCT cs.software.id) = :softwareCount
            ORDER BY c.roomNumber ASC
            """)
    List<Classroom> findClassroomsHavingAllSoftwares(
            @Param("softwareIds") List<Long> softwareIds,
            @Param("softwareCount") long softwareCount
    );
}