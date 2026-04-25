package com.ict.wiki.timetable.repository;

import com.ict.wiki.timetable.domain.Period;
import com.ict.wiki.timetable.domain.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> {

    /**
     * 주야 구분 + 교시 번호로 단건 조회
     * - 예: 주 1교시 → 09:00~09:50
     */
    Optional<Period> findByTypeAndPeriodNumber(PeriodType type, Integer periodNumber);

    /**
     * 주야 구분으로 전체 조회 (교시 순)
     */
    @Query("SELECT p FROM Period p WHERE p.type = :type ORDER BY p.periodNumber ASC")
    List<Period> findByTypeOrderByPeriodNumber(@Param("type") PeriodType type);

    /**
     * 전체 조회 (주야 구분 후 교시 순)
     */
    @Query("SELECT p FROM Period p ORDER BY p.type ASC, p.periodNumber ASC")
    List<Period> findAllOrdered();

    List<Period> findAllByOrderByTypeAscPeriodNumberAsc();
    List<Period> findByTypeOrderByPeriodNumberAsc(PeriodType type);
}