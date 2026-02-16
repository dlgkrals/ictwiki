package com.ict.wiki.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * - 세션 기반 인증
 * - CSRF 보호
 * - REST API 방식 로그인
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Spring Security 설정 초기화 시작");

        http
                // 일단 CSRF 비활성화 (Phase 3에서 활성화)
                .csrf(csrf -> csrf.disable())

                // 모든 요청 허용 (Phase 2에서 제한)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        log.info("Spring Security 설정 완료 - CSRF: 비활성화, 인증: 모든 요청 허용");
        return http.build();
    }

    /**
     * 비밀번호 암호화 (BCrypt)
     * - 기존 PasswordUtil과 동일한 방식
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCryptPasswordEncoder 빈 등록");
        return new BCryptPasswordEncoder();
    }
}