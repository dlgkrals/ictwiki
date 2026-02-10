package com.ict.wiki.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security의 BCryptPasswordEncoder 사용
 */
@Component
@RequiredArgsConstructor
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 암호화
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 비밀번호 검증
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 정적 메서드로 사용하는 레거시 코드 지원 (마이그레이션 기간 동안)
     * @deprecated Spring Bean 주입 받아서 사용하세요
     */
    @Deprecated
    public static String encodeStatic(String rawPassword) {
        // 임시로 새 BCryptPasswordEncoder 인스턴스 생성
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);
    }
}