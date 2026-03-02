package com.ict.wiki.login.repository;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== 기존 메서드들 ==========

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ========== STAFF 조회 (추가) ==========

    /**
     * 역할별 사용자 조회
     * - STAFF 목록 조회용
     */
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.name")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.active = true ORDER BY u.name")
    List<User> findByRoleIn(@Param("roles") List<Role> roles);

    long countByRoleIn(List<Role> roles);

    /**
     * 승인 대기 회원 목록
     */
    List<User> findByApprovedFalse();

    /**
     * 승인된 회원 수
     */
    long countByApprovedTrue();

    /**
     * 승인 대기 회원 수
     */
    long countByApprovedFalse();

    /**
     * 활성 회원 수
     */
    long countByActiveTrue();

    /**
     * 역할별 회원 수
     */
    long countByRole(Role role);
}