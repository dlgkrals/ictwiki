package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.Makeup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MakeupRepository extends JpaRepository<Makeup, Long> {

    /**
     * 단건 조회 (fetch join - LazyInitializationException 방지)
     */
    @Query("SELECT m FROM Makeup m JOIN FETCH m.classroom WHERE m.id = :id")
    Optional<Makeup> findByIdWithClassroom(@Param("id") Long id);

    /**
     * 날짜 범위로 보강 목록 조회 (달력 뷰용)
     */
    @Query("""
            SELECT m FROM Makeup m JOIN FETCH m.classroom
            WHERE m.date BETWEEN :startDate AND :endDate
            ORDER BY m.date ASC, m.startTime ASC
            """)
    List<Makeup> findByDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 날짜 보강 목록
     */
    @Query("""
            SELECT m FROM Makeup m JOIN FETCH m.classroom
            WHERE m.date = :date
            ORDER BY m.startTime ASC
            """)
    List<Makeup> findByDate(@Param("date") LocalDate date);

    /**
     * 충돌 체크: 특정 강의실 + 날짜 + 시간대 겹침 여부
     * - 정규수업 충돌 체크와 동일한 시간 겹침 로직
     */
    @Query("""
            SELECT m FROM Makeup m
            WHERE m.classroom.id = :classroomId
            AND m.date = :date
            AND m.startTime < :endTime
            AND m.endTime > :startTime
            """)
    List<Makeup> findConflicts(
            @Param("classroomId") Long classroomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * 충돌 체크 (자기 자신 제외 - 수정 시)
     */
    @Query("""
            SELECT m FROM Makeup m
            WHERE m.classroom.id = :classroomId
            AND m.date = :date
            AND m.startTime < :endTime
            AND m.endTime > :startTime
            AND m.id != :excludeId
            """)
    List<Makeup> findConflictsExcluding(
            @Param("classroomId") Long classroomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}