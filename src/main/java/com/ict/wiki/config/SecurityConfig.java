package com.ict.wiki.config;

import com.ict.wiki.security.auth.CustomAuthenticationProvider;
import com.ict.wiki.security.auth.CustomUserDetailsService;
import com.ict.wiki.security.filter.JsonAuthenticationFilter;
import com.ict.wiki.security.handler.LoginFailureHandler;
import com.ict.wiki.security.handler.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    private final CustomUserDetailsService customUserDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final PasswordEncoder passwordEncoder;  // ← PasswordEncoderConfig에서 주입

    /**
     * Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Spring Security 설정 초기화 시작");

        http
                // 커스텀 AuthenticationProvider 등록
                .authenticationProvider(customAuthenticationProvider())

                // JSON 로그인 필터 등록
                .addFilterBefore(jsonAuthenticationFilter(http),
                        UsernamePasswordAuthenticationFilter.class)

                // 일단 CSRF 비활성화 (Phase 3에서 활성화)
                .csrf(csrf -> csrf.disable())

                // URL 기반 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/check-email",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/test/public"
                        ).permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        log.info("Spring Security 설정 완료");
        return http.build();
    }

    /**
     * CustomAuthenticationProvider Bean
     */
    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        log.info("CustomAuthenticationProvider Bean 생성");
        return new CustomAuthenticationProvider(customUserDetailsService, passwordEncoder);
    }

    /**
     * JSON 로그인 필터 Bean
     */
    @Bean
    public JsonAuthenticationFilter jsonAuthenticationFilter(HttpSecurity http) throws Exception {
        AuthenticationManager authManager = authenticationManager(http);

        JsonAuthenticationFilter filter = new JsonAuthenticationFilter(authManager);
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        log.info("JsonAuthenticationFilter 등록 완료");
        return filter;
    }

    /**
     * AuthenticationManager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.authenticationProvider(customAuthenticationProvider());

        return authManagerBuilder.build();
    }
}