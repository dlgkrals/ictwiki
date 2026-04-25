package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.DayOfWeek;
import com.ict.wiki.timetable.domain.PeriodType;
import com.ict.wiki.timetable.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 단건 조회 (fetch join - LazyInitializationException 방지)
     */
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.classroom WHERE s.id = :id")
    Optional<Schedule> findByIdWithClassroom(@Param("id") Long id);

    /**
     * 미배정 수업 조회 (강의실 없는 것)
     */
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.semester = :semester AND s.classroom IS NULL
            ORDER BY s.dayOfWeek ASC, s.periodStart ASC
            """)
    List<Schedule> findUnassignedBySemester(@Param("semester") String semester);

    /**
     * 학기별 전체 시간표 조회
     */
    @Query("""
        SELECT s FROM Schedule s LEFT JOIN FETCH s.classroom
        WHERE s.semester = :semester
        ORDER BY CASE WHEN s.classroom IS NULL THEN 1 ELSE 0 END ASC,
                 s.classroom.roomNumber ASC, s.dayOfWeek ASC, s.periodStart ASC
        """)
    List<Schedule> findBySemester(@Param("semester") String semester);

    /**
     * 학기 + 강의실별 시간표 조회
     */
    @Query("""
            SELECT s FROM Schedule s JOIN FETCH s.classroom
            WHERE s.semester = :semester AND s.classroom.id = :classroomId
            ORDER BY s.dayOfWeek ASC, s.periodStart ASC
            """)
    List<Schedule> findBySemesterAndClassroomId(
            @Param("semester") String semester,
            @Param("classroomId") Long classroomId
    );

    /**
     * 충돌 체크: 특정 강의실 + 요일 + 시간대 겹침 여부
     * - 엑셀 SUMPRODUCT 수식과 동일한 로직
     * - 새 수업의 startTime < 기존 수업의 endTime AND 새 수업의 endTime > 기존 수업의 startTime
     */
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.semester = :semester
            AND s.classroom.id = :classroomId
            AND s.dayOfWeek = :dayOfWeek
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            """)
    List<Schedule> findConflicts(
            @Param("semester") String semester,
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * 충돌 체크 (자기 자신 제외 - 수정 시)
     */
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.semester = :semester
            AND s.classroom.id = :classroomId
            AND s.dayOfWeek = :dayOfWeek
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            AND s.id != :excludeId
            """)
    List<Schedule> findConflictsExcluding(
            @Param("semester") String semester,
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    /**
     * 학기 전체 삭제 (재업로드 시)
     */
    @Modifying
    @Query("DELETE FROM Schedule s WHERE s.semester = :semester")
    void deleteBySemester(@Param("semester") String semester);

    /**
     * 시간표 목록 가져오기
     */
    @Query("SELECT DISTINCT s.semester FROM Schedule s ORDER BY s.semester DESC")
    List<String> findAllSemesters();
}