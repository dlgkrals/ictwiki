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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

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
    private final PasswordEncoder passwordEncoder;

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

                // 세션 관리
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)  // 동시 세션 1개
                        .maxSessionsPreventsLogin(false)  // 새 로그인 시 기존 세션 무효화
                )

                // ⭐ CSRF 활성화 (로그인 포함 모든 POST/PUT/DELETE에 적용)
                .csrf(csrf -> {
                    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                    requestHandler.setCsrfRequestAttributeName("_csrf");

                    csrf
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            .csrfTokenRequestHandler(requestHandler)  // ← 이 줄 추가
                            .ignoringRequestMatchers(
                                    "/api/auth/signup",
                                    "/api/auth/verify-email",
                                    "/api/auth/resend-verification",
                                    "/api/auth/forgot-password",
                                    "/api/auth/reset-password"
                            );
                })

                // URL 기반 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/login",           // CSRF 검증 O, 인증 불필요
                                "/api/auth/signup",          // CSRF 검증 X, 인증 불필요
                                "/api/auth/check-email",     // GET이라 CSRF 무관
                                "/api/auth/verify-email",    // CSRF 검증 X, 인증 불필요
                                "/api/auth/resend-verification", // CSRF 검증 X
                                "/api/auth/forgot-password", // CSRF 검증 X
                                "/api/auth/reset-password",  // CSRF 검증 X
                                "/api/auth/csrf-token",      // GET이라 CSRF 무관
                                "/api/test/public"           // GET이라 CSRF 무관
                        ).permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        log.info("Spring Security 설정 완료 - CSRF: 활성화 (로그인 포함)");
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