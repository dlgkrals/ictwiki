package com.ict.wiki.login.domain;

import lombok.Getter;

@Getter
public enum Role {
    STUDENT("근로"),
    STAFF("사원"),
    MANAGER("책임"),
    TA("조교"),
    ADMIN("관리자");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}