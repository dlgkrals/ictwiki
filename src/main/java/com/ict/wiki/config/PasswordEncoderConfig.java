package com.ict.wiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder 설정
 * SecurityConfig와 분리하여 순환 참조 방지
 */
@Slf4j
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCryptPasswordEncoder 빈 등록");
        return new BCryptPasswordEncoder();
    }
}