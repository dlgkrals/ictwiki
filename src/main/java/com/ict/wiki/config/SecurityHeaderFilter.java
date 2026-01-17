package com.ict.wiki.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * 보안 헤더 설정 필터
 */
@Slf4j
public class SecurityHeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 클릭재킹 방지
        response.setHeader("X-Frame-Options", "DENY");

        // MIME 스니핑 방지
        response.setHeader("X-Content-Type-Options", "nosniff");

        // XSS 공격 방지
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // HTTPS 강제 (운영 환경에서 활성화)
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        // Referrer 정보 제어
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 브라우저 기능 접근 차단
        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), interest-cohort=()");

        // XSS, 코드 인젝션 공격 방지
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';");

        log.debug("보안 헤더 설정 완료 - URI: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
