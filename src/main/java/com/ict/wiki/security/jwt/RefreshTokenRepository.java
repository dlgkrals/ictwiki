package com.ict.wiki.security.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByEmail(String email);

    Optional<RefreshToken> findByToken(String token);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime now);

}