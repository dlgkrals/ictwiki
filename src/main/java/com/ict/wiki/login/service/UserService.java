package com.ict.wiki.login.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ict.wiki.exception.code.AuthErrorCode;
import com.ict.wiki.exception.custom.AuthException;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.UserUpdateRequest;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 사용자 관리 서비스
 * - 사용자 CRUD
 * - STAFF 목록 조회 (캐싱)
 * - 사용자 정보 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // STAFF 목록 캐시 (영구 유지, 변경 시에만 무효화)
    private final Cache<String, List<User>> staffCache = CacheBuilder.newBuilder()
            .maximumSize(1)  // STAFF 목록 1개만
            .build();

    private static final List<Role> WORKER_ROLES = Arrays.stream(Role.values())
            .filter(role -> role != Role.ADMIN)
            .toList();

    /**
     * 이메일로 사용자 조회
     *
     * @param email 이메일
     * @return 사용자
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.USER_NOT_FOUND));
    }

    /**
     * ID로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일 중복 확인
     *
     * @param email 이메일
     * @return 중복 여부
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자 저장
     *
     * @param user 사용자
     */
    @Transactional
    public void save(User user) {
        User savedUser = userRepository.save(user);
        log.info("사용자 저장 완료 - UserId: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

        // STAFF 역할이면 캐시 무효화
        if (WORKER_ROLES.contains(user.getRole())) {
            clearStaffCache();
        }

    }

    /**
     * 사용자 정보 수정 (이름, 전화번호)
     *
     * @param userId 사용자 ID
     * @param request 수정 요청
     * @return 수정된 사용자
     */
    @Transactional
    public User updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = findById(userId);

        // 이름 수정
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.updateName(request.getName());
        }

        // 전화번호 수정
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            String normalizedPhone = PhoneNumberUtil.normalize(request.getPhoneNumber());
            user.updatePhoneNumber(normalizedPhone);
        }

        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료 - UserId: {}, Name: {}", userId, request.getName());

        return updatedUser;
    }

    /**
     * 사용자 계정 활성화
     *
     * @param user 사용자
     */
    @Transactional
    public void activateUser(User user) {
        user.verify();
        userRepository.save(user);
        log.info("사용자 계정 활성화 완료 - Email: {}", user.getEmail());
    }

    /**
     * 비밀번호 업데이트
     *
     * @param user 사용자
     * @param encodedPassword 암호화된 비밀번호
     */
    @Transactional
    public void updatePassword(User user, String encodedPassword) {
        user.updatePassword(encodedPassword);
        userRepository.save(user);
        log.info("비밀번호 업데이트 완료 - UserId: {}, Email: {}", user.getId(), user.getEmail());
    }

    /**
     * STAFF 목록 조회 (캐싱)
     * - 첫 조회: DB에서 가져와서 캐싱
     * - 이후 조회: 캐시에서 반환 (쿼리 없음)
     * - 변경 시: 캐시 무효화 후 다시 DB 조회
     */
    public List<User> getStaffUsers() {
        try {
            return staffCache.get("staff", () -> {
                log.info("🔵 DB에서 STAFF 목록 조회 (캐시 미스)");
                return userRepository.findByRoleIn(WORKER_ROLES);
            });
        } catch (ExecutionException e) {
            log.error("STAFF 캐시 조회 실패, DB에서 직접 조회", e);
            return userRepository.findByRoleIn(WORKER_ROLES);
        }
    }

    /**
     * STAFF 캐시 무효화
     * - 회원가입, 역할 변경, 사용자 삭제 시 호출
     */
    public void clearStaffCache() {
        staffCache.invalidate("staff");
        log.info("🔴 STAFF 캐시 무효화");
    }

    /**
     * 역할 변경 (STAFF 승격/강등)
     *
     * @param userId 사용자 ID
     * @param newRole 새 역할
     * @return 변경된 사용자
     */
    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        User user = findById(userId);
        Role oldRole = user.getRole();

        // 역할 변경 로직
         user.updateRole(newRole);

        if (WORKER_ROLES.contains(oldRole) || WORKER_ROLES.contains(newRole)) {
            clearStaffCache();
        }

        log.info("사용자 역할 변경 - UserId: {}, 이전: {}, 이후: {}", userId, oldRole, newRole);

        return user;
    }

    /**
     * 사용자 삭제
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);

        userRepository.deleteById(userId);

        if (WORKER_ROLES.contains(user.getRole())) {
            clearStaffCache();
        }

        log.info("사용자 삭제 - UserId: {}", userId);
    }

    /**
     * 현재 캐시된 STAFF 수 (모니터링용)
     */
    public long getCachedStaffCount() {
        return staffCache.size();
    }

    /**
     * 캐시 통계 (개발/디버그용)
     */
    public String getCacheStats() {
        return String.format("STAFF Cache - Size: %d, Stats: %s",
                staffCache.size(),
                staffCache.stats());
    }
}