package com.ict.wiki.login.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Guava Cache 기반 비밀번호 재설정 서비스
 * - 1시간 자동 만료
 * - 1회용 토큰
 * - 5분 재전송 제한
 * - DB 불필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;  // ⭐ 추가

    // 재설정 토큰 캐시 (1시간 후 자동 만료)
    private final Cache<String, String> resetTokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    // 재전송 제한 캐시 (5분 후 자동 만료)
    private final Cache<String, LocalDateTime> resendTimeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    /**
     * 비밀번호 재설정 이메일 발송
     *
     * @param email 이메일
     */
    public void sendPasswordResetEmail(String email) {
        // 사용자 존재 확인
        User user = userService.findByEmail(email);

        // 계정 활성화 확인
        if (!user.isActive()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않은 계정입니다");
        }

        // ⭐ 5분 재전송 제한 체크
        checkResendLimit(email);

        // UUID 토큰 생성
        String token = UUID.randomUUID().toString();

        // 캐시에 저장 (token -> email)
        resetTokenCache.put(token, email);

        // 이메일 발송
        emailService.sendPasswordResetEmail(email, token);

        // 재전송 시간 기록
        resendTimeCache.put(email, LocalDateTime.now());

        log.info("비밀번호 재설정 메일 발송 - Email: {}, Token: {} (1시간 유효)", email, token);
    }

    /**
     * 재설정 토큰 검증
     *
     * @param token 재설정 토큰
     * @return 토큰이 유효한지 여부
     */
    public boolean validateResetToken(String token) {
        String email = resetTokenCache.getIfPresent(token);

        if (email == null) {
            log.warn("유효하지 않거나 만료된 재설정 토큰 - Token: {}", token);
            return false;
        }

        log.debug("재설정 토큰 검증 성공 - Token: {}, Email: {}", token, email);
        return true;
    }

    /**
     * 비밀번호 재설정
     *
     * @param token 재설정 토큰
     * @param newPassword 새 비밀번호
     */
    public void resetPassword(String token, String newPassword) {
        // 토큰에서 이메일 조회
        String email = resetTokenCache.getIfPresent(token);

        if (email == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 재설정 토큰입니다. 재설정 메일을 다시 요청해주세요.");
        }

        // 사용자 조회
        User user = userService.findByEmail(email);

        // ⭐ 기존 비밀번호와 동일한지 검증 (PasswordEncoder 사용)
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다");
        }

        // 비밀번호 암호화 및 업데이트 (PasswordEncoder 사용)
        String encodedPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(user, encodedPassword);

        // 토큰 즉시 삭제 (1회용)
        resetTokenCache.invalidate(token);

        // 재전송 제한도 초기화
        resendTimeCache.invalidate(email);

        log.info("비밀번호 재설정 완료 - Email: {}", email);
    }

    /**
     * 5분 재전송 제한 체크
     *
     * @param email 이메일
     */
    private void checkResendLimit(String email) {
        LocalDateTime lastSendTime = resendTimeCache.getIfPresent(email);

        if (lastSendTime != null) {
            long secondsSinceLastSend = Duration.between(lastSendTime, LocalDateTime.now()).getSeconds();

            if (secondsSinceLastSend < 300) { // 5분 = 300초
                long remainingSeconds = 300 - secondsSinceLastSend;
                long remainingMinutes = remainingSeconds / 60;
                long remainingSecondsInMinute = remainingSeconds % 60;

                throw new IllegalArgumentException(
                        String.format("비밀번호 재설정 메일은 5분에 한 번만 요청할 수 있습니다. %d분 %d초 후 다시 시도해주세요.",
                                remainingMinutes, remainingSecondsInMinute)
                );
            }
        }
    }

    /**
     * 현재 캐시에 저장된 토큰 개수 (모니터링용)
     *
     * @return 토큰 개수
     */
    public long getCachedTokenCount() {
        return resetTokenCache.size();
    }

    /**
     * 재전송까지 남은 시간 조회 (디버깅용)
     *
     * @param email 이메일
     * @return 남은 시간(초), 재전송 가능하면 0
     */
    public long getRemainingCooldownSeconds(String email) {
        LocalDateTime lastSendTime = resendTimeCache.getIfPresent(email);
        if (lastSendTime == null) {
            return 0;
        }

        long secondsSinceLastSend = Duration.between(lastSendTime, LocalDateTime.now()).getSeconds();
        return Math.max(0, 300 - secondsSinceLastSend);
    }

    /**
     * 캐시 전체 초기화 (관리자/테스트용)
     */
    public void clearAllTokens() {
        resetTokenCache.invalidateAll();
        resendTimeCache.invalidateAll();
        log.info("모든 비밀번호 재설정 토큰 캐시 초기화");
    }
}