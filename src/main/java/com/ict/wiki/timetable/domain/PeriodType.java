package com.ict.wiki.timetable.domain;

import lombok.Getter;

/**
 * 주간/야간 구분
 */
@Getter
public enum PeriodType {
    DAY("주"),
    NIGHT("야");

    private final String label;

    PeriodType(String label) {
        this.label = label;
    }

    public static PeriodType from(String label) {
        for (PeriodType type : values()) {
            if (type.label.equals(label)) return type;
        }
        throw new IllegalArgumentException("잘못된 주야 구분입니다: " + label);
    }
}