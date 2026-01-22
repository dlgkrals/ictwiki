package com.ict.wiki.login.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Guava Cache 기반 로그인 실패 시도 관리 서비스
 * 5회 실패 시 5분 잠금, 10회 실패 시 30분 잠금, 이후 계속 30분 잠금 반복
 */
@Service
@Slf4j
public class LoginAttemptService {

    // 실패 횟수 캐시 (1시간 후 자동 만료 - 충분히 길게 설정)
    private final LoadingCache<String, Integer> failCountCache;

    // 잠금 상태 캐시 (일반 Cache 사용 - null 허용)
    private final Cache<String, LocalDateTime> lockCache;

    public LoginAttemptService() {
        // 실패 횟수 캐시 (1시간 후 자동 삭제 - 잠금보다 길게!)
        failCountCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)  // ← 1시간으로 변경!
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });

        // 잠금 캐시 (30분 후 자동 삭제)
        lockCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 로그인 성공 - 모든 기록 초기화
     */
    public void loginSucceeded(String email) {
        failCountCache.invalidate(email);
        lockCache.invalidate(email);
        log.info("로그인 성공 - Email: {}, 실패 기록 초기화", email);
    }

    /**
     * 로그인 실패 기록
     */
    public void loginFailed(String email) {
        int attempts;
        try {
            attempts = failCountCache.get(email);
        } catch (ExecutionException e) {
            attempts = 0;
        }

        attempts++;
        failCountCache.put(email, attempts);

        // 5회 실패 시 5분 잠금
        if (attempts == 5) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(5);
            lockCache.put(email, lockUntil);
            log.warn("로그인 5회 실패 - Email: {}, 5분 잠금 시작", email);
        }
        // 10회 이상 실패 시 30분 잠금 (10, 15, 20, ... 모두 30분)
        else if (attempts >= 10 && attempts % 5 == 0) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
            lockCache.put(email, lockUntil);
            log.warn("로그인 {}회 실패 - Email: {}, 30분 잠금 시작", attempts, email);
        }

        log.warn("로그인 실패 - Email: {}, 실패 횟수: {}, 잠금 상태: {}",
                email, attempts, isLocked(email));
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isLocked(String email) {
        LocalDateTime lockUntil = lockCache.getIfPresent(email);

        if (lockUntil == null) {
            return false;
        }

        // 잠금 시간이 지났으면 잠금만 해제 (카운트는 유지!)
        if (LocalDateTime.now().isAfter(lockUntil)) {
            lockCache.invalidate(email);  // 잠금만 해제
            // failCountCache는 그대로 유지 → 누적 카운트 계속됨
            return false;
        }

        return true;
    }

    /**
     * 남은 잠금 시간 (초)
     */
    public long getRemainingLockTime(String email) {
        LocalDateTime lockUntil = lockCache.getIfPresent(email);

        if (lockUntil == null || !isLocked(email)) {
            return 0;
        }

        return java.time.Duration.between(LocalDateTime.now(), lockUntil).getSeconds();
    }

    /**
     * 실패 횟수 조회
     */
    public int getFailCount(String email) {
        try {
            return failCountCache.get(email);
        } catch (ExecutionException e) {
            return 0;
        }
    }

    /**
     * 잠금까지 남은 시도 횟수
     */
    public int getRemainingAttempts(String email) {
        int failedAttempts = getFailCount(email);

        if (failedAttempts < 5) {
            return 5 - failedAttempts;  // 첫 5회까지
        } else if (failedAttempts < 10) {
            return 10 - failedAttempts;  // 10회까지
        } else {
            // 10회 이상: 다음 5의 배수까지 남은 횟수
            int nextLockPoint = ((failedAttempts / 5) + 1) * 5;
            return nextLockPoint - failedAttempts;
        }
    }

    /**
     * 관리자용 - 강제 잠금 해제
     */
    public void unlockUser(String email) {
        failCountCache.invalidate(email);
        lockCache.invalidate(email);
        log.info("계정 잠금 강제 해제 - Email: {}", email);
    }

    /**
     * 관리자용 - 현재 추적 중인 계정 수
     */
    public long getTotalTrackedAccounts() {
        return failCountCache.size();
    }

    /**
     * 관리자용 - 현재 잠긴 계정 수
     */
    public long getLockedAccountsCount() {
        return lockCache.size();
    }
}