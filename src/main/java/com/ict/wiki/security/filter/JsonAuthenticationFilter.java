package com.ict.wiki.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.dto.request.LoginRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        // 3. Authentication 객체 생성
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, password);

        // 4. AuthenticationManager에게 인증 위임
        // → CustomAuthenticationProvider.authenticate() 호출됨
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}