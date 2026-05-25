package com.ict.wiki.security.auth;

import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security가 사용자 정보를 조회할 때 사용하는 서비스
 * loadUserByUsername() 메서드를 구현하여 DB에서 사용자 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * Security가 인증 시 호출하는 메서드
     * @param username 사용자 식별자 (우리는 email 사용)
     * @return UserDetails 구현체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 조회 시도 - Email: {}", username);

        User user;
        try {
            user = userService.findByEmail(username);
        } catch (Exception e) {
            log.warn("사용자 조회 실패 - Email: {}, 예외타입: {}, 메시지: {}",
                    username, e.getClass().getName(), e.getMessage(), e);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        log.info("DB 조회 성공 - userId: {}, active: {}, approved: {}",
                user.getId(), user.isActive(), user.isApproved());

        if (!user.isActive()) {
            log.warn("비활성 계정 로그인 시도 - Email: {}", username);
            throw new DisabledException("이메일 인증이 필요합니다");
        }

        if (!user.isApproved()) {
            log.warn("미승인 계정 로그인 시도 - Email: {}", username);
            throw new DisabledException("관리자 승인 대기 중입니다");
        }

        log.debug("사용자 조회 성공 - Email: {}, Role: {}", username, user.getRole());
        return new CustomUserDetails(user);
    }
}