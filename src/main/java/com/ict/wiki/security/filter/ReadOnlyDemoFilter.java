package com.ict.wiki.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.security.auth.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 데모(읽기 전용) 계정 쓰기 차단 필터
 * - 설정된 데모 이메일 계정이 변경 요청(POST/PUT/DELETE/PATCH)을 보내면 403
 * - 안전 메서드(GET/HEAD/OPTIONS)와 인증 경로(/api/auth/**)는 통과 → 로그인/로그아웃/조회는 정상
 * - app.demo.email 이 비어 있으면 아무 동작도 하지 않음
 */
@Slf4j
public class ReadOnlyDemoFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final String demoEmail;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReadOnlyDemoFilter(String demoEmail) {
        this.demoEmail = demoEmail;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (shouldBlock(request)) {
            log.warn("데모 계정 쓰기 차단 - {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "데모 계정은 읽기 전용입니다. 변경 작업은 허용되지 않습니다."));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldBlock(HttpServletRequest request) {
        // 데모 이메일 미설정 시 비활성화
        if (demoEmail == null || demoEmail.isBlank()) {
            return false;
        }
        // 안전 메서드는 통과 (조회 중 일어나는 조회수 증가 등 내부 쓰기에는 영향 없음)
        if (!MUTATING_METHODS.contains(request.getMethod())) {
            return false;
        }
        // 로그인/로그아웃 등 인증 경로는 통과
        if (request.getRequestURI().startsWith("/api/auth/")) {
            return false;
        }
        // 현재 인증 사용자가 데모 계정인지 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            // getUsername()은 복호화된 평문 이메일을 반환
            return demoEmail.equalsIgnoreCase(userDetails.getUsername());
        }
        return false;
    }
}