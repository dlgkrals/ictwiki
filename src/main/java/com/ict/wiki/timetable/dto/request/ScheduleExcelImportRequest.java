package com.ict.wiki.timetable.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleExcelImportRequest {
    private Integer priority;     // 순위
    private String department;    // 학과
    private Integer grade;        // 학년
    private String section;       // 반
    private String courseName;    // 교과목
    private String professor;     // 교수명
    private String softwareNote;  // 설치 소프트웨어
    private String optionStr;     // "O", "X"
    private Integer roomNumber;   // 강의실 번호
    private String periodType;    // "주", "야"
    private String dayOfWeek;     // "월","화","수","목","금"
    private String periodStr;     // "1~3", "5~7"
}