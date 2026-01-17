package com.ict.wiki.login.dto.request;

import lombok.Getter;

@Getter
public class LoginRequest {

    private String emailPrefix; // "student"
    private String password;

    public String getFullEmail() {
        return emailPrefix + "@g.seoil.ac.kr";
    }
}
