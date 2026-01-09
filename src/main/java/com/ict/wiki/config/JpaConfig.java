package com.ict.wiki.config;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
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

    private final UserRepository userRepository;

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

                // 세션에서 사용자 ID 가져오기
                Long userId = (Long) session.getAttribute(SESSION_USER_KEY);

                if (userId == null) {
                    return Optional.of("system");
                }

                // 사용자 정보 조회
                User user = userRepository.findById(userId).orElse(null);

                if (user == null) {
                    return Optional.of("system");
                }

                return Optional.of(user.getEmail());

            } catch (Exception e) {
                return Optional.of("system");
            }
        };
    }
}