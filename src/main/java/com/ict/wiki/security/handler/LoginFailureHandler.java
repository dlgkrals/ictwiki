package com.ict.wiki.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 실패 시 처리 핸들러
 * - JSON 에러 응답 반환
 * - 실패 원인별 메시지 구분
 */
@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.warn("로그인 실패 - Reason: {}, Message: {}",
                exception.getClass().getSimpleName(), exception.getMessage());

        // 1. 실패 원인별 메시지 설정
        String errorMessage = getErrorMessage(exception);

        // 2. JSON 에러 응답 작성
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        errorResponse.put("code", "AUTHENTICATION_FAILED");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * 예외 타입별 에러 메시지 반환
     */
    private String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof UsernameNotFoundException) {
            return "존재하지 않는 계정이거나 비활성화된 계정입니다";
        } else if (exception instanceof BadCredentialsException) {
            return exception.getMessage();
        } else if (exception instanceof DisabledException) {
            return exception.getMessage();
        } else {
            return "로그인에 실패했습니다";
        }
    }
}