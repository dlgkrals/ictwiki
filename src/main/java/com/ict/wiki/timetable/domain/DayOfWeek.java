package com.ict.wiki.timetable.domain;

import lombok.Getter;

/**
 * 요일
 */
@Getter
public enum DayOfWeek {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금");

    private final String label;

    DayOfWeek(String label) {
        this.label = label;
    }

    public static DayOfWeek from(String label) {
        for (DayOfWeek day : values()) {
            if (day.label.equals(label)) return day;
        }
        throw new IllegalArgumentException("잘못된 요일입니다: " + label);
    }
}