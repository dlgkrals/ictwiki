package com.ict.wiki.util;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ict.wiki.login.controller.AuthController.SESSION_USER_KEY;

/**
 * 세션에서 현재 로그인한 사용자 정보를 쉽게 가져오기 위한 헬퍼 클래스
 */
@Component
@RequiredArgsConstructor
public class SessionUtil {

    private final UserRepository userRepository;

    /**
     * 세션에서 현재 사용자 ID 가져오기
     */
    public Long getCurrentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute(SESSION_USER_KEY);
    }

    /**
     * 세션에서 현재 사용자 정보 가져오기
     */
    public User getCurrentUser(HttpSession session) {
        Long userId = getCurrentUserId(session);

        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 현재 사용자가 로그인 상태인지 확인
     */
    public boolean isAuthenticated(HttpSession session) {
        return getCurrentUserId(session) != null;
    }
}
