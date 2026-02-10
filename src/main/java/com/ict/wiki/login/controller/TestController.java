package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    /**
     * 누구나 접근 가능한 공개 엔드포인트
     */
    @GetMapping("/public")
    public String publicEndpoint() {
        return "누구나 접근 가능한 엔드포인트!!!!!!!!";
    }
}