package com.ict.wiki.login.service;

import com.ict.wiki.exception.custom.AuthException;
import com.ict.wiki.exception.code.AuthErrorCode;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.util.AesEncryptionUtil;
import com.ict.wiki.util.EmailHashUtil;
import com.ict.wiki.util.PasswordUtil;
import com.ict.wiki.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final EmailVerificationService emailVerificationService;
    private final EmailHashUtil emailHashUtil;

    public void signup(SignupRequest request) {
        String email = request.getEmail();

        if (userService.existsByEmail(email)) {
            throw AuthException.of(AuthErrorCode.EMAIL_DUPLICATE);
        }

        String normalizedPhone = PhoneNumberUtil.normalize(request.getPhoneNumber());

        User user = User.builder()
                .email(email)
                .password(PasswordUtil.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(normalizedPhone)
                .role(Role.STAFF)
                .active(false)
                .approved(false)
                .build();

        user.updateEmailHash(emailHashUtil.hash(email));

        emailVerificationService.sendVerificationEmail(email);
        userService.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String email = request.getEmail();

        // 1. 계정 잠금 확인
        if (loginAttemptService.isLocked(email)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(email);
            throw AuthException.of(AuthErrorCode.ACCOUNT_LOCKED, remainingSeconds / 60, remainingSeconds % 60);
        }

        // 2. 사용자 조회
        User user;
        try {
            user = userService.findByEmail(email);
        } catch (AuthException e) {
            loginAttemptService.loginFailed(email);
            handleLoginFailure(email);
            throw e;
        }

        // 3. 비밀번호 확인
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(email);
            handleLoginFailure(email);
            throw AuthException.of(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 4. 계정 활성화 확인
        if (!user.isActive()) {
            throw AuthException.of(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 5. 승인 여부 확인
        if (!user.isApproved()) {
            throw AuthException.of(AuthErrorCode.NOT_APPROVED);
        }

        loginAttemptService.loginSucceeded(email);
        return user;
    }

    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String email) {
        return userService.existsByEmail(email);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userService.findById(userId);

        if (!PasswordUtil.matches(currentPassword, user.getPassword())) {
            throw AuthException.of(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (PasswordUtil.matches(newPassword, user.getPassword())) {
            throw AuthException.of(AuthErrorCode.PASSWORD_SAME_AS_OLD);
        }

        String encodedPassword = PasswordUtil.encode(newPassword);
        userService.updatePassword(user, encodedPassword);

        log.info("비밀번호 변경 완료 - UserId: {}, Email: {}", userId, user.getEmail());
    }

    private void handleLoginFailure(String email) {
        if (loginAttemptService.isLocked(email)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(email);
            throw AuthException.of(AuthErrorCode.ACCOUNT_LOCKED, remainingSeconds / 60, remainingSeconds % 60);
        }

        int remainingAttempts = loginAttemptService.getRemainingAttempts(email);
        if (remainingAttempts > 0) {
            throw AuthException.of(AuthErrorCode.INVALID_CREDENTIALS, remainingAttempts);
        }
    }
}