package com.ict.wiki.security.auth;

import com.ict.wiki.login.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security의 UserDetails 구현체
 * User 엔티티를 Security가 이해할 수 있는 형태로 래핑
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * 권한 정보 반환
     * Role.ADMIN → ROLE_ADMIN 형태로 변환 (Security 규칙)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + user.getRole().name();
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Security는 username을 식별자로 사용
     * 우리는 email을 username으로 사용
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;  // 만료 정책 없음
    }

    /**
     * 계정 잠김 여부
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;  // 잠금 정책 없음
    }

    /**
     * 비밀번호 만료 여부
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 비밀번호 만료 정책 없음
    }

    /**
     * 계정 활성화 여부
     * User의 active 필드 사용
     */
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * 사용자 ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * 사용자 이름
     */
    public String getName() {
        return user.getName();
    }
}