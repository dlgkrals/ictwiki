package com.ict.wiki.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.global.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.warn("로그인 실패 - Reason: {}, Message: {}",
                exception.getClass().getSimpleName(), exception.getMessage());

        HttpStatus status = getHttpStatus(exception);
        String code = getErrorCode(exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "로그인에 실패했습니다";

        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(),
                ErrorResponse.of(status, code, message));
    }

    private HttpStatus getHttpStatus(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.UNAUTHORIZED;
    }

    private String getErrorCode(AuthenticationException exception) {
        if (exception instanceof UsernameNotFoundException) {
            return "AUTH_002";
        } else if (exception instanceof DisabledException) {
            return "AUTH_005";
        } else if (exception instanceof BadCredentialsException) {
            return "AUTH_003";
        }
        return "AUTH_000";
    }
}