package com.ict.wiki.security.auth;

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

    /**
     * 실제 인증 처리
     * @param authentication 인증 요청 정보 (email, password)
     * @return 인증 성공 시 Authentication 객체 (권한 정보 포함)
     * @throws AuthenticationException 인증 실패 시
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();

        log.debug("인증 시도 - Email: {}", email);

        // 1. 사용자 조회
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        // 비활성 사용자 체크
        if (!userDetails.isEnabled()) {
            log.warn("비활성 사용자 - Email: {}", email);
            throw new DisabledException("비활성화된 계정입니다");
        }

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("비밀번호 불일치 - Email: {}", email);
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");
        }

        log.info("인증 성공 - Email: {}, Role: {}", email, userDetails.getUser().getRole());

        // 3. 인증 성공 객체 반환
        return new UsernamePasswordAuthenticationToken(
                userDetails,  // principal (인증된 사용자 정보)
                password,     // credentials
                userDetails.getAuthorities()  // authorities (권한 목록)
        );
    }

    /**
     * 이 Provider가 처리할 수 있는 Authentication 타입 지정
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}