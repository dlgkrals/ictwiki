package com.ict.wiki.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.util.CsrfTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSRF(Cross-Site Request Forgery) 방지 필터
 * 상태 변경 요청(POST, PUT, DELETE, PATCH)에 대해 CSRF 토큰을 검증합니다.
 */
@Slf4j
public class CsrfFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // CSRF 토큰 헤더 이름
    private static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    // CSRF 검증이 필요한 HTTP 메서드
    private static final List<String> CSRF_PROTECTED_METHODS = Arrays.asList(
            "POST", "PUT", "DELETE", "PATCH"
    );

    // CSRF 검증 제외 경로 (로그인, 회원가입 등)
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/check-email",
            "/api/auth/verify-email",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/resend-verification"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        log.debug("CSRF 필터 실행 - Method: {}, URI: {}", method, requestURI);

        // GET, HEAD, OPTIONS 등 안전한 메서드는 검증 제외
        if (!CSRF_PROTECTED_METHODS.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 제외 경로는 검증 제외
        if (isExcludePath(requestURI)) {
            log.debug("CSRF 검증 제외 경로: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 세션 확인
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("CSRF 검증 실패 - 세션 없음, URI: {}", requestURI);
            sendCsrfError(response, "세션이 만료되었습니다");
            return;
        }

        // 요청 헤더에서 CSRF 토큰 추출
        String requestToken = request.getHeader(CSRF_HEADER_NAME);

        // 토큰 검증
        if (!CsrfTokenUtil.validateToken(session, requestToken)) {
            log.warn("CSRF 검증 실패 - 토큰 불일치, URI: {}, Method: {}", requestURI, method);
            sendCsrfError(response, "CSRF 토큰이 유효하지 않습니다");
            return;
        }

        log.debug("CSRF 검증 성공 - URI: {}", requestURI);
        filterChain.doFilter(request, response);
    }

    /**
     * 제외 경로 확인
     */
    private boolean isExcludePath(String requestURI) {
        return EXCLUDE_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * 403 Forbidden 응답 전송
     */
    private void sendCsrfError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("code", "CSRF_TOKEN_INVALID");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
