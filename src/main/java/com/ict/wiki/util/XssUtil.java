package com.ict.wiki.util;

import java.util.HashMap;
import java.util.Map;

/**
 * XSS(Cross-Site Scripting) 방지 유틸리티
 * HTML 특수문자 이스케이핑 및 위험한 패턴 필터링
 */
public class XssUtil {

    // HTML 특수문자 매핑
    private static final Map<Character, String> HTML_ESCAPE_MAP = new HashMap<>();

    static {
        HTML_ESCAPE_MAP.put('<', "&lt;");
        HTML_ESCAPE_MAP.put('>', "&gt;");
        HTML_ESCAPE_MAP.put('&', "&amp;");
        HTML_ESCAPE_MAP.put('"', "&quot;");
        HTML_ESCAPE_MAP.put('\'', "&#x27;");
        HTML_ESCAPE_MAP.put('/', "&#x2F;");
    }

    /**
     * HTML 특수문자 이스케이핑
     * @param input 원본 문자열
     * @return 이스케이핑된 문자열
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder escaped = new StringBuilder(input.length() + 20);
        for (char c : input.toCharArray()) {
            String replacement = HTML_ESCAPE_MAP.get(c);
            if (replacement != null) {
                escaped.append(replacement);
            } else {
                escaped.append(c);
            }
        }
        return escaped.toString();
    }

    /**
     * 위험한 스크립트 패턴 제거
     * @param input 원본 문자열
     * @return 필터링된 문자열
     */
    public static String removeScriptTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 대소문자 구분 없이 <script> 태그 제거
        String filtered = input.replaceAll("(?i)<script[^>]*>.*?</script>", "");

        // 이벤트 핸들러 제거 (onclick, onerror 등)
        filtered = filtered.replaceAll("(?i)on\\w+\\s*=", "");

        // javascript: 프로토콜 제거
        filtered = filtered.replaceAll("(?i)javascript:", "");

        return filtered;
    }

    /**
     * SQL Injection 패턴 검사
     * @param input 검사할 문자열
     * @return 위험한 패턴 포함 여부
     */
    public static boolean containsSqlInjectionPattern(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();

        // 기본적인 SQL Injection 패턴들
        String[] dangerousPatterns = {
                "' or ", "\" or ", "' and ", "\" and ",
                "union select", "drop table", "insert into",
                "delete from", "update set", "exec ", "execute ",
                "script>", "<script", "javascript:", "onerror="
        };

        for (String pattern : dangerousPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 안전한 문자열인지 검증
     * @param input 검사할 문자열
     * @param allowHtml HTML 허용 여부
     * @return 안전한 문자열 여부
     */
    public static boolean isSafeString(String input, boolean allowHtml) {
        if (input == null || input.isEmpty()) {
            return true;
        }

        // SQL Injection 패턴 체크
        if (containsSqlInjectionPattern(input)) {
            return false;
        }

        // HTML이 허용되지 않는 경우 태그 존재 체크
        if (!allowHtml && (input.contains("<") || input.contains(">"))) {
            return false;
        }

        return true;
    }

    /**
     * 파일명 안전화 (경로 순회 공격 방지)
     * @param filename 원본 파일명
     * @return 안전한 파일명
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        // 경로 구분자 제거
        String safe = filename.replaceAll("[/\\\\]", "");

        // 상위 디렉토리 접근 패턴 제거
        safe = safe.replaceAll("\\.\\.", "");

        // 특수문자 제거 (한글, 영문, 숫자, 언더스코어, 하이픈, 점만 허용)
        safe = safe.replaceAll("[^가-힣a-zA-Z0-9._-]", "");

        return safe;
    }
}
