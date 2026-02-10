package com.ict.wiki.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    /**
     * JPA Auditing을 위한 현재 사용자 제공
     * Spring Security의 SecurityContext에서 사용자 정보를 가져옵니다
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                // Spring Security의 SecurityContext에서 인증 정보 가져오기
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // 인증되지 않았거나 익명 사용자인 경우
                if (authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of("system");
                }

                // 인증된 사용자의 이메일 반환
                return Optional.of(authentication.getName());

            } catch (Exception e) {
                // 에러 발생 시 system으로 처리
                return Optional.of("system");
            }
        };
    }
}