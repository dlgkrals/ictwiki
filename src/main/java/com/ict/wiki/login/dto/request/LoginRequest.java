package com.ict.wiki.login.dto.request;

import lombok.Data;
import lombok.Getter;

@Data
public class LoginRequest {

    private String emailPrefix; // "student"
    private String password;

    public String getFullEmail() {
        return emailPrefix + "@g.seoil.ac.kr";
    }
}
