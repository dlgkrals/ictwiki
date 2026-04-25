package com.ict.wiki.timetable.service;

import com.ict.wiki.timetable.domain.Period;
import com.ict.wiki.timetable.domain.PeriodType;
import com.ict.wiki.timetable.repository.PeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PeriodService {

    private final PeriodRepository periodRepository;

    public List<Period> getAllPeriods() {
        return periodRepository.findAllByOrderByTypeAscPeriodNumberAsc();
    }

    public List<Period> getPeriodsByType(PeriodType type) {
        return periodRepository.findByTypeOrderByPeriodNumberAsc(type);
    }
}