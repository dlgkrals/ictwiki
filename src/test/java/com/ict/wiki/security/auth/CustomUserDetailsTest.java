package com.ict.wiki.security.auth;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomUserDetails 단위 테스트")
class CustomUserDetailsTest {

    @Test
    @DisplayName("User 엔티티를 CustomUserDetails로 변환")
    void createCustomUserDetails() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스트")
                .phoneNumber("010-1234-5678")
                .role(Role.STAFF)
                .active(true)
                .approved(true)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // then
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getName()).isEqualTo("테스트");
        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getRole()).isEqualTo(Role.STAFF);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("권한이 ROLE_ 접두사와 함께 반환되어야 함")
    void getAuthorities() {
        // given
        User adminUser = User.builder()
                .email("admin@test.com")
                .password("password")
                .name("관리자")
                .role(Role.ADMIN)
                .active(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(adminUser);

        // when
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("비활성 사용자는 isEnabled가 false를 반환")
    void inactiveUser() {
        // given
        User inactiveUser = User.builder()
                .email("inactive@test.com")
                .password("password")
                .name("비활성")
                .role(Role.STUDENT)
                .active(false)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(inactiveUser);

        // then
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("계정 만료/잠금 정책이 없으므로 항상 true")
    void accountStatus() {
        // given
        User user = User.builder()
                .email("user@test.com")
                .password("password")
                .name("유저")
                .role(Role.STAFF)
                .active(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // then
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}