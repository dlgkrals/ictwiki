package com.ict.wiki.timetable.service;

import com.ict.wiki.timetable.domain.SoftwareMatchFailure;
import com.ict.wiki.timetable.dto.response.SoftwareMatchFailureResponse;
import com.ict.wiki.timetable.repository.SoftwareMatchFailureRepository;
import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoftwareMatchFailureService {

    private final SoftwareMatchFailureRepository failureRepository;

    /**
     * 학기별 매칭 실패 목록 조회
     */
    public List<SoftwareMatchFailureResponse> getFailures(String semester) {
        return failureRepository.findBySemesterOrderByRoomNumberAsc(semester)
                .stream()
                .map(SoftwareMatchFailureResponse::from)
                .toList();
    }

    /**
     * 학기 실패 내역 전체 교체 (재실행 시)
     */
    @Transactional
    public void replaceFailures(String semester, List<SoftwareMatchFailure> failures) {
        failureRepository.deleteBySemester(semester);
        failureRepository.saveAll(failures);
    }

    /**
     * 단건 해결 처리
     */
    @Transactional
    public SoftwareMatchFailureResponse resolve(Long id) {
        SoftwareMatchFailure failure = failureRepository.findById(id)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.SCHEDULE_NOT_FOUND));
        failure.resolve();
        return SoftwareMatchFailureResponse.from(failure);
    }
}
