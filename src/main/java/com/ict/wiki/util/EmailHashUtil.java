package com.ict.wiki.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class EmailHashUtil {

    public String hash(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    email.toLowerCase().trim().getBytes(StandardCharsets.UTF_8)
            );
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("이메일 해시 생성 실패", e);
        }
    }
}