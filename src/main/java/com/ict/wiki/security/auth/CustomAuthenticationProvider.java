package com.ict.wiki.security.auth;

import com.ict.wiki.login.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 커스텀 인증 Provider
 * - 비밀번호 검증 로직 수행
 * - 기존 AuthService.login()의 인증 로직을 Security 방식으로 이관
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;  // ⭐ 추가

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();

        log.debug("인증 시도 - Email: {}", email);

        // ⭐ 1. 계정 잠금 확인 (가장 먼저!)
        if (loginAttemptService.isLocked(email)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(email);
            long remainingMinutes = remainingSeconds / 60;

            log.warn("잠금된 계정 로그인 시도 - Email: {}", email);
            throw new BadCredentialsException(
                    String.format("로그인 시도 횟수를 초과했습니다. %d분 %d초 후 다시 시도해주세요.",
                            remainingMinutes, remainingSeconds % 60)
            );
        }

        // 2. 사용자 조회
        // 2. 사용자 조회
        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        } catch (DisabledException e) {
            // 비활성/미승인 계정은 실패 카운트 올리지 않고 그대로 전달
            throw e;
        } catch (Exception e) {
            // 이메일 없음 → 실패 카운트
            loginAttemptService.loginFailed(email);
            handleLoginFailure(email);
            throw e;
        }

        // 3. 비활성 사용자 체크
        if (!userDetails.isEnabled()) {
            log.warn("비활성 사용자 - Email: {}", email);
            throw new DisabledException("이메일 인증이 필요합니다");
        }

        // ⭐ 4. 승인 여부 확인
        if (!userDetails.getUser().isApproved()) {
            log.warn("미승인 사용자 - Email: {}", email);
            throw new DisabledException("관리자 승인 대기 중입니다");
        }

        // 5. 비밀번호 검증
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("비밀번호 불일치 - Email: {}", email);

            // ⭐ 비밀번호 틀림 → 실패 기록
            loginAttemptService.loginFailed(email);
            handleLoginFailure(email);

            throw new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // ⭐ 6. 로그인 성공 → 실패 기록 초기화
        loginAttemptService.loginSucceeded(email);

        log.info("인증 성공 - Email: {}, Role: {}", email, userDetails.getUser().getRole());

        // 7. 인증 성공 객체 반환
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                password,
                userDetails.getAuthorities()
        );
    }

    // ⭐ 실패 후 잠금 여부 체크 및 적절한 메시지 throw
    private void handleLoginFailure(String email) {
        if (loginAttemptService.isLocked(email)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(email);
            long remainingMinutes = remainingSeconds / 60;
            throw new BadCredentialsException(
                    String.format("로그인 시도 횟수를 초과했습니다. %d분 %d초 후 다시 시도해주세요.",
                            remainingMinutes, remainingSeconds % 60)
            );
        }

        int remainingAttempts = loginAttemptService.getRemainingAttempts(email);
        if (remainingAttempts > 0) {
            throw new BadCredentialsException(
                    String.format("이메일 또는 비밀번호가 일치하지 않습니다. (남은 시도: %d회)",
                            remainingAttempts)
            );
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}