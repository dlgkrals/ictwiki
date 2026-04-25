package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.SoftwareMatchFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SoftwareMatchFailureRepository extends JpaRepository<SoftwareMatchFailure, Long> {

    List<SoftwareMatchFailure> findBySemesterOrderByRoomNumberAsc(String semester);

    @Modifying
    @Query("DELETE FROM SoftwareMatchFailure f WHERE f.semester = :semester")
    void deleteBySemester(@Param("semester") String semester);
}
