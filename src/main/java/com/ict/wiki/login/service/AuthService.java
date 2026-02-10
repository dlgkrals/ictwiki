package com.ict.wiki.login.service;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.request.SignupRequest;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.security.jwt.JwtTokenProvider;
import com.ict.wiki.security.jwt.RefreshTokenService;
import com.ict.wiki.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    // JWT 관련
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final LoginAttemptService loginAttemptService;

    /**
     * 회원가입
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 1. 이메일 중복 확인
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.STUDENT)
                .active(false)  // 이메일 인증 전까지 비활성화
                .build();

        userService.save(user);
        log.info("회원가입 완료 - Email: {}", request.getEmail());

        // 4. 인증 메일 발송
        emailVerificationService.sendVerificationEmail(user.getEmail());
    }

    /**
     * 로그인 (JWT)
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        String email = request.getEmail();

        // 1. 로그인 시도 횟수 확인
        if (loginAttemptService.isLocked(email)) {
            long remainingTime = loginAttemptService.getRemainingLockTime(email);
            throw new IllegalStateException(
                    String.format("로그인 시도 횟수 초과. %d초 후 다시 시도하세요.", remainingTime)
            );
        }

        try {
            // 2. Spring Security 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            // 3. 사용자 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

            // 4. JWT 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

            // 5. Refresh Token DB 저장
            refreshTokenService.saveOrUpdate(user.getEmail(), refreshToken);

            // 6. Refresh Token 쿠키 설정
            long refreshTokenMaxAge = jwtTokenProvider.getRefreshTokenValidity() / 1000; // 초 단위
            cookieUtil.addRefreshTokenCookie(response, refreshToken, refreshTokenMaxAge);

            // 7. 로그인 성공 처리
            loginAttemptService.loginSucceeded(email);

            log.info("로그인 성공 - Email: {}, Role: {}", user.getEmail(), user.getRole());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenValidity() / 1000) // 초 단위
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();

        } catch (AuthenticationException e) {
            // 로그인 실패 처리
            loginAttemptService.loginFailed(email);
            log.warn("로그인 실패 - Email: {}, Reason: {}", email, e.getMessage());
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * Access Token 재발급
     */
    @Transactional(readOnly = true)
    public LoginResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        // 2. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken) ||
                !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 3. 이메일 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 4. DB에 저장된 Refresh Token과 비교
        if (!refreshTokenService.validateRefreshToken(refreshToken, email)) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        // 5. 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 6. 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());

        log.info("Access Token 재발급 - Email: {}", email);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidity() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String email, HttpServletResponse response) {
        // 1. DB에서 Refresh Token 삭제
        refreshTokenService.deleteByEmail(email);

        // 2. 쿠키에서 Refresh Token 삭제
        cookieUtil.deleteRefreshTokenCookie(response);

        log.info("로그아웃 완료 - Email: {}", email);
    }
}