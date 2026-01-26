package com.ict.wiki.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.ict.wiki.login.controller.AuthController.SESSION_USER_KEY;

@Slf4j
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 필터를 적용하지 않을 경로들 (인증 없이 접근 가능)
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/check-email",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/test/public"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("인증 필터 실행 - URI: {}, Method: {}", requestURI, method);

        // 제외 경로는 필터 통과
        if (isExcludePath(requestURI)) {
            log.debug("인증 제외 경로: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 세션 체크
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SESSION_USER_KEY) == null) {
            log.warn("인증 실패 - URI: {}, Session: {}", requestURI, session != null ? "존재" : "없음");

            // 인증되지 않은 요청
            sendUnauthorizedResponse(response, "로그인이 필요합니다");
            return;
        }

        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);
        log.debug("인증 성공 - UserId: {}, URI: {}", userId, requestURI);

        // 인증 통과
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
     * 401 Unauthorized 응답 전송
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("code", "UNAUTHORIZED");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}