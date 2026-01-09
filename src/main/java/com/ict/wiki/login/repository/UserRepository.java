package com.ict.wiki.login.repository;

import com.ict.wiki.login.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 인증이 필요한 사용자 조회 (3개월 이상 미인증)
    List<User> findByLastVerifiedDateBefore(LocalDate date);
}
