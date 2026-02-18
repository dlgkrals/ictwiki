package com.ict.wiki.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("로그인 통합 테스트")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private String csrfToken;
    private Cookie csrfCookie;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 사용자 생성
        User user = User.builder()
                .email("test@g.seoil.ac.kr")
                .password(passwordEncoder.encode("password123"))
                .name("테스트유저")
                .phoneNumber("010-1234-5678")
                .role(Role.STAFF)
                .active(true)
                .approved(true)
                .build();
        userRepository.save(user);

        // CSRF 토큰 발급 및 세션/쿠키 저장
        MvcResult result = mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isOk())
                .andReturn();

        // 세션 저장
        session = (MockHttpSession) result.getRequest().getSession();

        // CSRF 쿠키 저장
        csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");

        // CSRF 토큰 값 추출
        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> response = objectMapper.readValue(responseBody, Map.class);
        csrfToken = response.get("csrfToken");
    }

    @Test
    @DisplayName("올바른 자격증명으로 로그인 성공")
    void login_Success() throws Exception {
        // given
        Map<String, String> loginRequest = Map.of(
                "email", "test@g.seoil.ac.kr",
                "password", "password123"
        );

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .session(session)  // ← 세션 공유
                        .cookie(csrfCookie)  // ← 쿠키 공유
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@g.seoil.ac.kr"))  // ← 수정
                .andExpect(jsonPath("$.user.role").value("STAFF"));  // ← 수정
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void login_WrongPassword() throws Exception {
        // given
        Map<String, String> loginRequest = Map.of(
                "email", "test@g.seoil.ac.kr",
                "password", "wrongPassword"
        );

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .session(session)  // ← 세션 공유
                        .cookie(csrfCookie)  // ← 쿠키 공유
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 실패")
    void login_UserNotFound() throws Exception {
        // given
        Map<String, String> loginRequest = Map.of(
                "email", "notfound@g.seoil.ac.kr",
                "password", "password123"
        );

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .session(session)  // ← 세션 공유
                        .cookie(csrfCookie)  // ← 쿠키 공유
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}