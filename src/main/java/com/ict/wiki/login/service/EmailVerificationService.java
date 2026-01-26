package com.ict.wiki.login.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Guava Cache 기반 이메일 인증 서비스
 * - 30분 자동 만료
 * - 1회용 토큰
 * - DB 불필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // 인증 토큰 캐시 (30분 후 자동 만료)
    private final Cache<String, String> verificationTokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(10000)  // 최대 10,000개 토큰 저장
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 이미 인증된 경우
        if (user.isActive()) {
            log.warn("이미 인증된 사용자 - Email: {}", email);
            // 토큰 삭제
            verificationTokenCache.invalidate(token);
            return true;
        }

        // 계정 활성화 + 인증 날짜 업데이트
        user.verify();
        userRepository.save(user);

        // 토큰 즉시 삭제 (1회용)
        verificationTokenCache.invalidate(token);

        log.info("이메일 인증 완료 - Email: {}", email);
        return true;
    }

    /**
     * 인증 메일 재발송
     *
     * @param email 이메일
     */
    public void resendVerificationEmail(String email) {
        // 사용자 존재 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다"));

        // 이미 인증된 경우
        if (user.isActive()) {
            throw new IllegalArgumentException("이미 인증된 계정입니다");
        }

        // 재발송 (새 토큰 발급)
        sendVerificationEmail(email);
        log.info("인증 메일 재발송 - Email: {}", email);
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
     * 캐시 전체 초기화 (관리자/테스트용)
     */
    public void clearAllTokens() {
        verificationTokenCache.invalidateAll();
        log.info("모든 인증 토큰 캐시 초기화");
    }
}