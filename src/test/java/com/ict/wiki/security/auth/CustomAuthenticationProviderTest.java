package com.ict.wiki.security.auth;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationProvider 단위 테스트")
class CustomAuthenticationProviderTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomAuthenticationProvider authenticationProvider;

    @Test
    @DisplayName("올바른 자격증명으로 인증 성공")
    void authenticate_Success() {
        // given
        String email = "test@g.seoil.ac.kr";
        String password = "password123";
        String encodedPassword = "encodedPassword";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("테스트")
                .phoneNumber("010-1234-5678")
                .role(Role.STAFF)
                .active(true)
                .approved(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, password);

        // when
        Authentication result = authenticationProvider.authenticate(authRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isInstanceOf(CustomUserDetails.class);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_STAFF");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 인증 실패")
    void authenticate_WrongPassword() {
        // given
        String email = "test@g.seoil.ac.kr";
        String wrongPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("테스트")
                .phoneNumber("010-1234-5678")
                .role(Role.STAFF)
                .active(true)
                .approved(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
        given(passwordEncoder.matches(wrongPassword, encodedPassword)).willReturn(false);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, wrongPassword);

        // when & then
        assertThatThrownBy(() -> authenticationProvider.authenticate(authRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("비활성 사용자 인증 실패")
    void authenticate_DisabledUser() {
        // given
        String email = "inactive@g.seoil.ac.kr";
        String password = "password";
        String encodedPassword = "encodedPassword";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("비활성")
                .phoneNumber("010-1234-5678")
                .role(Role.STAFF)
                .active(false)  // 비활성
                .approved(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, password);

        // when & then
        assertThatThrownBy(() -> authenticationProvider.authenticate(authRequest))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("비활성화된 계정입니다");
    }

    @Test
    @DisplayName("UsernamePasswordAuthenticationToken을 지원함")
    void supports() {
        // when
        boolean result = authenticationProvider.supports(UsernamePasswordAuthenticationToken.class);

        // then
        assertThat(result).isTrue();
    }
}