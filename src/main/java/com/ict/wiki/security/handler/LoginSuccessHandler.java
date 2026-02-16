package com.ict.wiki.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.service.SessionManagementService;
import com.ict.wiki.security.auth.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 성공 시 처리 핸들러
 * - JSON 응답 반환
 * - 세션 관리 (중복 로그인 방지)
 * - CSRF 토큰 발급 (Phase 3에서 활성화)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SessionManagementService sessionManagementService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        log.info("로그인 성공 - Email: {}, Role: {}", email, userDetails.getUser().getRole());

        // 2. 세션 생성 및 중복 로그인 방지
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();

        // 기존 세션 무효화 및 새 세션 등록
        sessionManagementService.registerSession(email, sessionId);

        // 세션 타임아웃 설정 (30분)
        session.setMaxInactiveInterval(30 * 60);

        log.debug("세션 생성 완료 - SessionId: {}, Email: {}", sessionId, email);

        // 3. JSON 응답 작성
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", LoginResponse.from(userDetails.getUser()));

        // CSRF 토큰은 Phase 3에서 추가
        // responseBody.put("csrfToken", csrfToken);

        objectMapper.writeValue(response.getWriter(), responseBody);

        log.debug("로그인 응답 전송 완료 - Email: {}", email);
    }
}