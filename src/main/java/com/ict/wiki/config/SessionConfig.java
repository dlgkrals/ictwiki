package com.ict.wiki.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Session 설정
 * 세션을 중앙에서 관리하여 모든 세션에 접근 가능
 */
@Configuration
@EnableSpringHttpSession
public class SessionConfig {

    /**
     * 메모리 기반 세션 저장소
     * 모든 세션을 중앙에서 관리
     */
    @Bean
    public SessionRepository<? extends Session> sessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }
}