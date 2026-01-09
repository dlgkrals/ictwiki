package com.ict.wiki.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 비밀번호 암호화 유틸리티
 * BCrypt 알고리즘을 사용하여 비밀번호를 안전하게 암호화하고 검증합니다.
 */
public class PasswordUtil {

    // BCrypt의 salt rounds (복잡도)
    // 값이 클수록 더 안전하지만 느림 (일반적으로 10-12 사용)
    private static final int SALT_ROUNDS = 10;

    /**
     * 평문 비밀번호를 BCrypt로 암호화
     * @param plainPassword 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    public static String encode(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(SALT_ROUNDS));
    }

    /**
     * 평문 비밀번호와 암호화된 비밀번호를 비교
     * @param plainPassword 평문 비밀번호
     * @param hashedPassword 암호화된 비밀번호
     * @return 일치 여부
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}