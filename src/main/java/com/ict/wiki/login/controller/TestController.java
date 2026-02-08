package com.ict.wiki.login.controller;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ict.wiki.login.controller.AuthController.SESSION_USER_KEY;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    /**
     * 인증된 사용자만 접근 가능한 엔드포인트
     */
    @GetMapping("/hello")
    public String hello(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);

        if (userId == null) {
            return "로그인이 필요합니다";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return "안녕하세요, " + user.getName() + "님!";
    }

    /**
     * 누구나 접근 가능한 공개 엔드포인트
     */
    @GetMapping("/public")
    public String publicEndpoint() {
        return "누구나 접근 가능한 엔드포인트!!!";
    }
}