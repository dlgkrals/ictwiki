package com.ict.wiki.login.service;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;
    private final EmailVerificationService emailVerificationService;


    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     */
    public void signup(SignupRequest request) {
        String fullEmail = request.getFullEmail();

        // 이메일 중복 체크
        if (userRepository.existsByEmail(fullEmail)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .email(fullEmail)
                .password(PasswordUtil.encode(request.getPassword()))
                .name(request.getName())
                .studentId(request.getStudentId())
                .department(request.getDepartment())
                .role(Role.STUDENT)
                .active(false)
                .lastVerifiedDate(null)
                .build();

        emailVerificationService.sendVerificationEmail(fullEmail);
        userRepository.save(user);
    }

    /**
     * 로그인
     * @param request 로그인 요청 정보
     * @return 인증된 사용자 정보
     */
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String fullEmail = request.getFullEmail();

        // ⭐ 1. 계정 잠금 확인
        if (loginAttemptService.isLocked(fullEmail)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(fullEmail);
            long remainingMinutes = remainingSeconds / 60;

            throw new IllegalArgumentException(
                    String.format("로그인 시도 횟수를 초과했습니다. %d분 %d초 후 다시 시도해주세요.",
                            remainingMinutes, remainingSeconds % 60)
            );
        }

        // 2. 사용자 조회
        User user = userRepository.findByEmail(fullEmail)
                .orElseThrow(() -> {
                    // ⭐ 사용자 없음 → 실패 기록
                    loginAttemptService.loginFailed(fullEmail);

                    // 실패 후 잠금 여부 확인
                    if (loginAttemptService.isLocked(fullEmail)) {
                        long remainingSeconds = loginAttemptService.getRemainingLockTime(fullEmail);
                        long remainingMinutes = remainingSeconds / 60;
                        return new IllegalArgumentException(
                                String.format("로그인 시도 횟수를 초과했습니다. %d분 %d초 후 다시 시도해주세요.",
                                        remainingMinutes, remainingSeconds % 60)
                        );
                    }

                    int remainingAttempts = loginAttemptService.getRemainingAttempts(fullEmail);
                    if (remainingAttempts > 0) {
                        return new IllegalArgumentException(
                                String.format("이메일 또는 비밀번호가 일치하지 않습니다. (남은 시도: %d회)",
                                        remainingAttempts)
                        );
                    } else {
                        return new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다");
                    }
                });

        // 3. 비밀번호 확인
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            // ⭐ 비밀번호 틀림 → 실패 기록
            loginAttemptService.loginFailed(fullEmail);

            // ⭐ 실패 후 잠금 여부 확인 (5회 또는 10회에 도달했을 수 있음)
            if (loginAttemptService.isLocked(fullEmail)) {
                long remainingSeconds = loginAttemptService.getRemainingLockTime(fullEmail);
                long remainingMinutes = remainingSeconds / 60;
                throw new IllegalArgumentException(
                        String.format("로그인 시도 횟수를 초과했습니다. %d분 %d초 후 다시 시도해주세요.",
                                remainingMinutes, remainingSeconds % 60)
                );
            }

            int remainingAttempts = loginAttemptService.getRemainingAttempts(fullEmail);
            if (remainingAttempts > 0) {
                throw new IllegalArgumentException(
                        String.format("이메일 또는 비밀번호가 일치하지 않습니다. (남은 시도: %d회)",
                                remainingAttempts)
                );
            } else {
                throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다");
            }
        }

        // 4. 계정 활성화 확인
        if (!user.isActive()) {
            throw new IllegalArgumentException("이메일 인증 필요");
        }

        // 5. 마지막 인증 날짜 확인 (3개월)
        if (user.getLastVerifiedDate() == null ||
                user.getLastVerifiedDate().isBefore(LocalDate.now().minusMonths(3))) {
            throw new IllegalArgumentException("계정 재인증이 필요합니다. 이메일을 확인해주세요");
        }

        // ⭐ 6. 로그인 성공 → 실패 기록 초기화
        loginAttemptService.loginSucceeded(fullEmail);

        return user;
    }

    /**
     * 이메일 중복 체크
     * @param emailPrefix 이메일 앞부분 (도메인 제외)
     * @return 중복 여부
     */
    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String emailPrefix) {
        String fullEmail = emailPrefix + "@g.seoil.ac.kr";
        return userRepository.existsByEmail(fullEmail);
    }

    /**
     * 비밀번호 변경 (로그인 상태)
     *
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // ✅ 현재 비밀번호 확인
        if (!PasswordUtil.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // ✅ 기존 비밀번호와 동일한지 검증 (재사용 로직)
        if (PasswordUtil.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다");
        }

        // ✅ 비밀번호 암호화 및 업데이트 (재사용 로직)
        String encodedPassword = PasswordUtil.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료 - UserId: {}, Email: {}", userId, user.getEmail());
    }
}