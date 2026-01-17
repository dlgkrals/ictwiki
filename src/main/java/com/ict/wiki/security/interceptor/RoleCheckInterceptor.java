package com.ict.wiki.security.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.ict.wiki.login.controller.AuthController.SESSION_USER_KEY;

/**
 * 권한 기반 접근 제어를 위한 인터셉터
 * @RequireRole 애노테이션이 붙은 메서드의 권한을 체크합니다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCheckInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // HandlerMethod가 아니면 통과 (정적 리소스 등)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // @RequireRole 애노테이션이 없으면 통과
        if (requireRole == null) {
            return true;
        }

        // 세션에서 사용자 ID 가져오기
        HttpSession session = request.getSession(false);
        if (session == null) {
            sendForbiddenResponse(response, "인증이 필요합니다");
            return false;
        }

        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            sendForbiddenResponse(response, "인증이 필요합니다");
            return false;
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 권한 체크
        Role[] allowedRoles = requireRole.value();
        boolean hasPermission = Arrays.asList(allowedRoles).contains(user.getRole());

        if (!hasPermission) {
            log.warn("권한 없음 - UserId: {}, UserRole: {}, RequiredRoles: {}, URI: {}",
                    userId, user.getRole(), Arrays.toString(allowedRoles), request.getRequestURI());
            sendForbiddenResponse(response, "접근 권한이 없습니다");
            return false;
        }

        log.debug("권한 체크 통과 - UserId: {}, Role: {}, URI: {}",
                userId, user.getRole(), request.getRequestURI());

        return true;
    }

    /**
     * 403 Forbidden 응답 전송
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("code", "FORBIDDEN");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}



