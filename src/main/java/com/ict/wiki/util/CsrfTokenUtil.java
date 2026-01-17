package com.ict.wiki.util;

import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF(Cross-Site Request Forgery) 토큰 관리 유틸리티
 */
public class CsrfTokenUtil {

    private static final String CSRF_TOKEN_SESSION_KEY = "CSRF_TOKEN";
    private static final int TOKEN_LENGTH = 32; // 32바이트 = 256비트
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * CSRF 토큰 생성
     * @param session HTTP 세션
     * @return 생성된 토큰
     */
    public static String generateToken(HttpSession session) {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // 세션에 토큰 저장
        session.setAttribute(CSRF_TOKEN_SESSION_KEY, token);

        return token;
    }

    /**
     * 세션에서 CSRF 토큰 조회
     * @param session HTTP 세션
     * @return 저장된 토큰 (없으면 null)
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(CSRF_TOKEN_SESSION_KEY);
    }

    /**
     * CSRF 토큰 검증
     * @param session HTTP 세션
     * @param token 클라이언트가 보낸 토큰
     * @return 검증 통과 여부
     */
    public static boolean validateToken(HttpSession session, String token) {
        if (session == null || token == null) {
            return false;
        }

        String sessionToken = getToken(session);
        if (sessionToken == null) {
            return false;
        }

        // 타이밍 공격 방지를 위한 상수 시간 비교
        return constantTimeEquals(sessionToken, token);
    }

    /**
     * 상수 시간 문자열 비교 (타이밍 공격 방지)
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * CSRF 토큰 제거
     * @param session HTTP 세션
     */
    public static void removeToken(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CSRF_TOKEN_SESSION_KEY);
        }
    }
}
