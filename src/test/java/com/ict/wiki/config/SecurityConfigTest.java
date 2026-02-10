package com.ict.wiki.config;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Spring Security 설정 테스트")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 (트랜잭션으로 자동 롤백됨)
        User testUser = User.builder()
                .email("test@g.seoil.ac.kr")
                .password(passwordEncoder.encode("test1234"))
                .name("테스트관리자")
                .phoneNumber("010-0000-0000")
                .role(Role.ADMIN)
                .active(true)
                .approved(true)
                .build();

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 보호된 엔드포인트 접근 가능")
    void accessProtectedEndpointWithValidToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@g.seoil.ac.kr\",\"password\":\"test1234\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@g.seoil.ac.kr"));
    }

    @Test
    @DisplayName("로그인 시 Access Token과 Refresh Token 쿠키가 발급됨")
    void loginReturnsTokensAndCookie() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@g.seoil.ac.kr\",\"password\":\"test1234\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@g.seoil.ac.kr"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("인증 없이 공개 엔드포인트 접근 가능")
    void publicEndpointAccessWithoutAuth() throws Exception {
        // /api/auth/login은 인증 없이 접근 가능해야 함
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@example.com\",\"password\":\"test\"}"))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // 400 or 401 (로그인 실패는 정상)
    }

    @Test
    @DisplayName("인증 없이 보호된 엔드포인트 접근 시 401 반환")
    void protectedEndpointAccessWithoutAuth() throws Exception {
        // 인증이 필요한 엔드포인트는 401 반환
        mockMvc.perform(get("/api/users/me")  // ⭐ 실제 존재하는 보호된 엔드포인트
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CORS 헤더가 정상적으로 설정됨")
    void corsHeadersAreSet() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("보안 헤더가 정상적으로 설정됨")
    void securityHeadersAreSet() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andDo(print())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 접근 시 401 반환")
    void invalidJwtTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me")  // ⭐ 실제 존재하는 보호된 엔드포인트
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}