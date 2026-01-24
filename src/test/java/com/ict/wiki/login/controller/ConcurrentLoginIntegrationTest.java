package com.ict.wiki.login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.login.service.SessionManagementService;
import com.ict.wiki.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 동시 로그인 방지 통합 테스트 (단순화)
 *
 * Note: MockMvc 환경에서는 실제 HTTP 세션 쿠키와 SessionRepository 연동에 제한이 있습니다.
 * 세션 관리 로직의 상세한 테스트는 SessionManagementServiceTest(단위 테스트)에서 수행됩니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("동시 로그인 방지 통합 테스트")
class ConcurrentLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionManagementService sessionManagementService;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        User testUser = User.builder()
                .email("test@g.seoil.ac.kr")
                .password(PasswordUtil.encode("Test123!@#"))
                .name("테스트")
                .studentId("12345678")
                .department("컴퓨터공학과")
                .role(Role.STUDENT)
                .active(true)
                .lastVerifiedDate(LocalDate.now())
                .build();
        userRepository.save(testUser);

        // 로그인 요청 DTO
        loginRequest = new LoginRequest();
        loginRequest.setEmailPrefix("test");
        loginRequest.setPassword("Test123!@#");

        // 세션 관리 초기화
        sessionManagementService.clearAll();
    }

    @Test
    @DisplayName("로그인 성공 시 세션 등록")
    void login_RegistersSession() throws Exception {
        // when
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@g.seoil.ac.kr"))
                .andExpect(jsonPath("$.csrfToken").exists());

        // then - 세션이 관리 서비스에 등록되었는지 확인
        assertThat(sessionManagementService.isLoggedIn("test@g.seoil.ac.kr")).isTrue();
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 계정으로 2번 로그인 시 세션 1개만 유지")
    void duplicateLogin_KeepsOnlyOneSession() throws Exception {
        // given - 첫 번째 로그인
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(1);
        String firstSessionId = sessionManagementService.getActiveSession("test@g.seoil.ac.kr");

        // when - 두 번째 로그인
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // then - 여전히 세션 1개만 유지
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(1);

        // 새 세션 ID로 교체되었는지 확인
        String secondSessionId = sessionManagementService.getActiveSession("test@g.seoil.ac.kr");
        assertThat(secondSessionId).isNotEqualTo(firstSessionId);
    }

    @Test
    @DisplayName("여러 사용자 동시 로그인")
    void multipleUsers_ConcurrentLogin() throws Exception {
        // given - 추가 사용자 생성
        User user2 = User.builder()
                .email("test2@g.seoil.ac.kr")
                .password(PasswordUtil.encode("Test123!@#"))
                .name("테스트2")
                .studentId("87654321")
                .department("컴퓨터공학과")
                .role(Role.STUDENT)
                .active(true)
                .lastVerifiedDate(LocalDate.now())
                .build();
        userRepository.save(user2);

        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setEmailPrefix("test2");
        loginRequest2.setPassword("Test123!@#");

        // when - 두 사용자 로그인
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest2)))
                .andExpect(status().isOk());

        // then - 각 사용자마다 세션 1개씩
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(2);
        assertThat(sessionManagementService.isLoggedIn("test@g.seoil.ac.kr")).isTrue();
        assertThat(sessionManagementService.isLoggedIn("test2@g.seoil.ac.kr")).isTrue();
    }

    @Test
    @DisplayName("같은 사용자 3번 연속 로그인 - 세션 1개 유지")
    void sameUser_ThreeLogins_KeepsOnlyOne() throws Exception {
        // when - 3번 로그인
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        // then - 세션 1개만 유지
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(1);
        assertThat(sessionManagementService.isLoggedIn("test@g.seoil.ac.kr")).isTrue();
    }
}