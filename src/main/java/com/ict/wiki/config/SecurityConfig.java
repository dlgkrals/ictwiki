package com.ict.wiki.config;

import com.ict.wiki.login.service.LoginAttemptService;
import com.ict.wiki.security.auth.CustomAuthenticationProvider;
import com.ict.wiki.security.auth.CustomUserDetailsService;
import com.ict.wiki.security.filter.JsonAuthenticationFilter;
import com.ict.wiki.security.handler.LoginFailureHandler;
import com.ict.wiki.security.handler.LoginSuccessHandler;
import com.ict.wiki.security.handler.RestAccessDeniedHandler;
import com.ict.wiki.security.handler.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
    private final LoginAttemptService loginAttemptService;

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Value("${app.csp.connect-src}")
    private String connectSrc;

    /**
     * Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Spring Security 설정 초기화 시작");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 커스텀 AuthenticationProvider 등록
                .authenticationProvider(customAuthenticationProvider())

                // JSON 로그인 필터 등록
                .addFilterBefore(jsonAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)

                // 세션 관리
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().newSession()
                        .maximumSessions(1)  // 동시 세션 1개
                        .maxSessionsPreventsLogin(false)  // 새 로그인 시 기존 세션 무효화
                )

                // ⭐ CSRF 활성화 (로그인 포함 모든 POST/PUT/DELETE에 적용)
                .csrf(csrf -> {
                    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                    requestHandler.setCsrfRequestAttributeName("_csrf");

                    csrf
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            .csrfTokenRequestHandler(requestHandler)
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
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/check-email",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/csrf-token",
                                "/api/test/public"
                        ).permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                                "camera=(), microphone=(), geolocation=(), interest-cohort=()"))
                        .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy",
                                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; font-src 'self'; connect-src " + connectSrc + ";"))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
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
        return new CustomAuthenticationProvider(customUserDetailsService, passwordEncoder, loginAttemptService);
    }

    /**
     * AuthenticationManager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(customAuthenticationProvider()));
    }

    /**
     * JSON 로그인 필터 Bean
     */
    @Bean
    public JsonAuthenticationFilter jsonAuthenticationFilter() {
        JsonAuthenticationFilter filter = new JsonAuthenticationFilter(authenticationManager());
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        log.info("JsonAuthenticationFilter 등록 완료");
        return filter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(
                "https://www.ictwiki.site",
                "http://localhost:5173",
                "http://localhost:4173",
                "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-CSRF-TOKEN", "X-XSRF-TOKEN"));
        config.setExposedHeaders(Arrays.asList("X-CSRF-TOKEN", "X-XSRF-TOKEN"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}