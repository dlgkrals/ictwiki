package com.ict.wiki.login.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Guava Cache 기반 이메일 인증 서비스
 * - 30분 자동 만료
 * - 1회용 토큰
 * - 60초 재전송 제한
 * - 최대 3회 재전송 제한
 * - DB 불필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final UserService userService;  // ✅ UserRepository → UserService
    private final EmailService emailService;

    // 인증 토큰 캐시 (30분 후 자동 만료)
    private final Cache<String, String> verificationTokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    // 재전송 횟수 캐시 (60초 후 자동 초기화)
    private final Cache<String, Integer> resendCountCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(10000)
            .build();

    // 마지막 재전송 시간 캐시 (60초 후 자동 만료)
    private final Cache<String, LocalDateTime> resendTimeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(10000)
            .build();

    /**
     * 인증 메일 발송
     *
     * @param email 이메일
     */
    public void sendVerificationEmail(String email) {
        // UUID 토큰 생성
        String token = UUID.randomUUID().toString();

        // 캐시에 저장 (token -> email)
        verificationTokenCache.put(token, email);

        // 이메일 발송
        emailService.sendVerificationEmail(email, token);

        log.info("인증 메일 발송 - Email: {}, Token: {} (30분 유효)", email, token);
    }

    /**
     * 이메일 인증 처리
     *
     * @param token 인증 토큰
     * @return 인증 성공 여부
     */
    public boolean verifyEmail(String token) {
        // 캐시에서 이메일 조회
        String email = verificationTokenCache.getIfPresent(token);

        if (email == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 인증 토큰입니다. 인증 메일을 재발송해주세요.");
        }

        // 사용자 조회
        User user = userService.findByEmail(email);  // ✅ 변경

        // 이미 인증된 경우
        if (user.isActive()) {
            log.warn("이미 인증된 사용자 - Email: {}", email);
            // 토큰 삭제
            verificationTokenCache.invalidate(token);
            return true;
        }

        // 계정 활성화 + 인증 날짜 업데이트
        userService.activateUser(user);  // ✅ 변경

        // 토큰 즉시 삭제 (1회용)
        verificationTokenCache.invalidate(token);

        // ⭐ 인증 성공 시 재전송 제한 초기화
        clearResendLimit(email);

        log.info("이메일 인증 완료 - Email: {}", email);
        return true;
    }

    /**
     * 인증 메일 재발송 (60초 제한 + 최대 3회)
     *
     * @param email 이메일
     */
    public void resendVerificationEmail(String email) {
        // 사용자 존재 확인
        User user = userService.findByEmail(email);  // ✅ 변경

        // 이미 인증된 경우
        if (user.isActive()) {
            throw new IllegalArgumentException("이미 인증된 계정입니다");
        }

        // ⭐ 재전송 제한 체크
        checkResendLimit(email);

        // 재발송
        sendVerificationEmail(email);

        // ⭐ 재전송 카운트 증가
        incrementResendCount(email);
    }

    /**
     * 재전송 제한 체크
     *
     * @param email 이메일
     */
    private void checkResendLimit(String email) {
        // 1. 60초 내 재전송 체크
        LocalDateTime lastSendTime = resendTimeCache.getIfPresent(email);
        if (lastSendTime != null) {
            long secondsSinceLastSend = Duration.between(lastSendTime, LocalDateTime.now()).getSeconds();
            if (secondsSinceLastSend < 60) {
                long remainingSeconds = 60 - secondsSinceLastSend;
                throw new IllegalArgumentException(
                        String.format("이메일은 60초에 한 번만 재전송할 수 있습니다. %d초 후 다시 시도해주세요.", remainingSeconds)
                );
            }
        }

        // 2. 최대 3번 제한 체크
        Integer sendCount = resendCountCache.getIfPresent(email);
        if (sendCount != null && sendCount >= 3) {
            throw new IllegalArgumentException("이메일 재전송은 최대 3회까지 가능합니다. 60초 후 다시 시도해주세요.");
        }
    }

    /**
     * 재전송 카운트 증가
     *
     * @param email 이메일
     */
    private void incrementResendCount(String email) {
        Integer currentCount = resendCountCache.getIfPresent(email);
        int newCount = (currentCount == null) ? 1 : currentCount + 1;

        resendCountCache.put(email, newCount);
        resendTimeCache.put(email, LocalDateTime.now());

        log.info("이메일 재전송 - Email: {}, 횟수: {}/3, 남은 횟수: {}",
                email, newCount, 3 - newCount);
    }

    /**
     * 재전송 제한 초기화 (인증 성공 시)
     *
     * @param email 이메일
     */
    private void clearResendLimit(String email) {
        resendCountCache.invalidate(email);
        resendTimeCache.invalidate(email);
        log.debug("재전송 제한 초기화 - Email: {}", email);
    }

    /**
     * 현재 캐시에 저장된 토큰 개수 (모니터링용)
     *
     * @return 토큰 개수
     */
    public long getCachedTokenCount() {
        return verificationTokenCache.size();
    }

    /**
     * 재전송 남은 횟수 조회 (관리자/디버깅용)
     *
     * @param email 이메일
     * @return 남은 재전송 횟수
     */
    public int getRemainingResendCount(String email) {
        Integer currentCount = resendCountCache.getIfPresent(email);
        return 3 - (currentCount == null ? 0 : currentCount);
    }

    /**
     * 재전송까지 남은 시간 조회 (관리자/디버깅용)
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
        return Math.max(0, 60 - secondsSinceLastSend);
    }

    /**
     * 캐시 전체 초기화 (관리자/테스트용)
     */
    public void clearAllTokens() {
        verificationTokenCache.invalidateAll();
        resendCountCache.invalidateAll();
        resendTimeCache.invalidateAll();
        log.info("모든 인증 토큰 및 재전송 제한 캐시 초기화");
    }
}