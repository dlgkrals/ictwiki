package com.ict.wiki.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Refresh Token 저장 또는 업데이트
     */
    @Transactional
    public void saveOrUpdate(String email, String token) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenValidity() / 1000);

        refreshTokenRepository.findByEmail(email)
                .ifPresentOrElse(
                        existingToken -> existingToken.updateToken(token, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .email(email)
                                        .token(token)
                                        .expiresAt(expiresAt)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        )
                );

        log.info("Refresh Token 저장 완료: email={}", email);
    }

    /**
     * Refresh Token 검증
     */
    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String token, String email) {
        return refreshTokenRepository.findByToken(token)
                .map(refreshToken ->
                        refreshToken.getEmail().equals(email) &&
                                !refreshToken.isExpired() &&
                                jwtTokenProvider.validateToken(token) &&
                                jwtTokenProvider.isRefreshToken(token)
                )
                .orElse(false);
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     */
    @Transactional
    public void deleteByEmail(String email) {
        refreshTokenRepository.deleteByEmail(email);
        log.info("Refresh Token 삭제 완료: email={}", email);
    }

    /**
     * 만료된 토큰 정리 (스케줄링)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("만료된 Refresh Token 정리 완료");
    }
}