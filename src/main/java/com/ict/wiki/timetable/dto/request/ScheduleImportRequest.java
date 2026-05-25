package com.ict.wiki.timetable.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleImportRequest {
    private String department;    // 학과
    private Integer grade;        // 학년
    private String courseName;    // 교과목
    private String softwareNote;  // 설치 소프트웨어
    private String dayOfWeek;     // "월","화","수","목","금"
    private String periodStr;     // "1~3", "5~7"
    private String periodType;    // "주", "야"
    private String section;       // 반
    private String professor;     // 신청교수명
    private String optionStr;     // "O", "X"
}