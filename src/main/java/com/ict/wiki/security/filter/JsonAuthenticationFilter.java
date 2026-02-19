package com.ict.wiki.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.dto.request.LoginRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;

/**
 * JSON 형식 로그인 요청을 처리하는 커스텀 필터
 * - application/json 요청 본문 파싱
 * - UsernamePasswordAuthenticationToken 생성
 * - AuthenticationManager에게 인증 위임
 */
@Slf4j
public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * /api/auth/login POST 요청만 처리
     */
    public JsonAuthenticationFilter(AuthenticationManager authenticationManager) {
        // ✅ Builder 패턴 사용
        super(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, "/api/auth/login"));
        setAuthenticationManager(authenticationManager);
    }

    /**
     * 인증 시도
     * - JSON 요청 본문을 LoginRequest DTO로 파싱
     * - Authentication 객체 생성 후 AuthenticationManager에게 위임
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        log.info("========== 로그인 요청 시작 ==========");

        // 세션 정보 로그
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("기존 세션 ID: {}", session.getId());
            log.info("세션 생성 시간: {}", new java.util.Date(session.getCreationTime()));
        } else {
            log.info("기존 세션 없음");
        }

        // CSRF 토큰 로그
        String csrfHeader = request.getHeader("X-XSRF-TOKEN");
        Cookie[] cookies = request.getCookies();
        String csrfCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("XSRF-TOKEN".equals(cookie.getName())) {
                    csrfCookie = cookie.getValue();
                    break;
                }
            }
        }

        log.info("헤더의 CSRF 토큰: {}", csrfHeader);
        log.info("쿠키의 CSRF 토큰: {}", csrfCookie);
        log.info("CSRF 토큰 일치 여부: {}", csrfHeader != null && csrfHeader.equals(csrfCookie));

        log.debug("JSON 로그인 필터 실행 - URI: {}", request.getRequestURI());

        // 1. Content-Type 검증
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            log.warn("잘못된 Content-Type - Expected: application/json, Actual: {}", contentType);
            throw new AuthenticationServiceException("Content-Type must be application/json");
        }

        // 2. JSON 요청 본문 파싱
        LoginRequest loginRequest;
        try {
            loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            log.error("JSON 파싱 실패", e);
            throw new AuthenticationServiceException("Failed to parse login request", e);
        }

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        log.debug("로그인 요청 파싱 완료 - Email: {}", email);
        log.info("========== 로그인 요청 파싱 완료 ==========");

        // 3. Authentication 객체 생성
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, password);

        //request attribute에 rememberMe 저장 (RememberMeServices가 읽음)
        if (loginRequest.isRememberMe()) {
            request.setAttribute("rememberMe", "true");
        }

        // 4. AuthenticationManager에게 인증 위임
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}