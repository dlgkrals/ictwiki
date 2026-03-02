package com.ict.wiki.admin.service;

import com.ict.wiki.admin.dto.response.UserManagementResponse;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 회원 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserService userService;

    private static final List<Role> WORKER_ROLES = Arrays.stream(Role.values())
            .filter(role -> role != Role.ADMIN)
            .toList();

    // ========== 회원 조회 ==========

    /**
     * 전체 회원 목록
     */
    public List<UserManagementResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserManagementResponse::from)
                .toList();
    }

    /**
     * 승인 대기 회원 목록 (approved = false)
     */
    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    /**
     * 특정 회원 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }

    // ========== 회원 승인 ==========

    /**
     * 회원 승인
     * - approved = true
     * - active = true
     */
    @Transactional
    public void approveUser(Long userId) {
        User user = getUserById(userId);

        if (user.isApproved()) {
            throw new IllegalArgumentException("이미 승인된 회원입니다");
        }

        user.approve();
        userRepository.save(user);

        log.info("회원 승인 완료 - UserId: {}, Email: {}", userId, user.getEmail());
    }

    /**
     * 회원 승인 거부
     * - approved = false
     * - active = false
     */
    @Transactional
    public void rejectUser(Long userId) {
        User user = getUserById(userId);

        user.reject();
        userRepository.save(user);

        log.info("회원 승인 거부 - UserId: {}, Email: {}", userId, user.getEmail());
    }

    // ========== 계정 활성화/비활성화 ==========

    /**
     * 계정 활성화
     */
    @Transactional
    public void activateUser(Long userId) {
        User user = getUserById(userId);

        if (!user.isApproved()) {
            throw new IllegalArgumentException("승인되지 않은 회원은 활성화할 수 없습니다");
        }

        user.activate();
        userRepository.save(user);
        userService.clearStaffCache();

        log.info("계정 활성화 - UserId: {}, Email: {}", userId, user.getEmail());
    }

    /**
     * 계정 비활성화
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);

        user.deactivate();
        userRepository.save(user);
        userService.clearStaffCache();

        log.info("계정 비활성화 - UserId: {}, Email: {}", userId, user.getEmail());
    }

    // ========== 비밀번호 변경 ==========

    /**
     * 관리자가 회원 비밀번호 강제 변경
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = getUserById(userId);

        // 비밀번호 암호화
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        user.changePasswordByAdmin(hashedPassword);
        userRepository.save(user);

        log.info("관리자 비밀번호 변경 - UserId: {}, Email: {}", userId, user.getEmail());
    }

    // ========== 권한 변경 ==========

    /**
     * 회원 권한 변경
     */
    @Transactional
    public void changeRole(Long userId, Role newRole) {
        User user = getUserById(userId);
        Role oldRole = user.getRole();

        // UserService의 updateUserRole 사용 (캐시 처리 포함)
        userService.updateUserRole(userId, newRole);

        log.info("권한 변경 - UserId: {}, Email: {}, {} → {}",
                userId, user.getEmail(), oldRole, newRole);
    }

    // ========== 회원 삭제 ==========

    /**
     * 회원 삭제 (소프트 삭제)
     * - active = false
     * - approved = false
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        // 소프트 삭제
        user.deactivate();
        user.reject();
        userRepository.save(user);
        userService.clearStaffCache();

        log.info("회원 삭제 (소프트) - UserId: {}, Email: {}", userId, user.getEmail());
    }

    // ========== 통계 ==========

    /**
     * 회원 통계
     */
    public Map<String, Object> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long pendingUsers = userRepository.countByApprovedFalse();
        long workerCount = userRepository.countByRoleIn(WORKER_ROLES);
        long adminCount = userRepository.countByRole(Role.ADMIN);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("pendingUsers", pendingUsers);
        stats.put("workerCount", workerCount);
        stats.put("adminCount", adminCount);

        return stats;
    }
}