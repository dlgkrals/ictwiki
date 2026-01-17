package com.ict.wiki.config;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static com.ict.wiki.login.controller.AuthController.SESSION_USER_KEY;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaConfig {

    private final SessionUtil sessionUtil;  // ✅ SessionUtil 주입

    /**
     * JPA Auditing을 위한 현재 사용자 제공
     * 세션에서 사용자 정보를 가져옵니다
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                // 현재 요청의 세션 가져오기
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes == null) {
                    return Optional.of("system");
                }

                HttpServletRequest request = attributes.getRequest();
                HttpSession session = request.getSession(false);

                if (session == null) {
                    return Optional.of("system");
                }

                // ✅ SessionUtil 사용 - 기존의 복잡한 코드 대체!
                User user = sessionUtil.getCurrentUser(session);
                return Optional.of(user.getEmail());

            } catch (Exception e) {
                // 로그인하지 않은 경우 또는 에러 발생 시
                return Optional.of("system");
            }
        };
    }
}