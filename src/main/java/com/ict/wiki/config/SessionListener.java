package com.ict.wiki.config;

import com.ict.wiki.login.service.SessionManagementService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 세션 생성/소멸 이벤트 처리 리스너
 * 세션 타임아웃 시 자동으로 세션 관리 맵에서 제거
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionListener implements HttpSessionListener {

    private final SessionManagementService sessionManagementService;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.debug("세션 생성 - SessionId: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();

        // 세션 관리 서비스에서 제거
        sessionManagementService.removeSession(sessionId);

        log.debug("세션 소멸 - SessionId: {}", sessionId);
    }
}