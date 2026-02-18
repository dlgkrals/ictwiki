package com.ict.wiki.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.security.auth.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 성공 시 처리 핸들러
 * - JSON 응답 반환
 * - 세션 관리 (중복 로그인 방지)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        log.info("로그인 성공 - Email: {}, Role: {}", email, userDetails.getUser().getRole());



        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", LoginResponse.from(userDetails.getUser()));
        responseBody.put("csrfToken", csrfToken.getToken());

        objectMapper.writeValue(response.getWriter(), responseBody);

        log.debug("로그인 응답 전송 완료 - Email: {}", email);
    }
}