package com.ict.wiki.util;

public class PhoneNumberUtil {

    /**
     * 전화번호 정규화 (숫자만 추출)
     * 010-1234-5678 → 01012345678
     */
    public static String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}