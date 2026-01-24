package com.ict.wiki.login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionManagementService 테스트")
class SessionManagementServiceTest {

    @Mock
    private SessionRepository<MapSession> sessionRepository;

    @InjectMocks
    private SessionManagementService sessionManagementService;

    private String testEmail;
    private String sessionId1;
    private String sessionId2;

    @BeforeEach
    void setUp() {
        testEmail = "test@g.seoil.ac.kr";
        sessionId1 = "session-111";
        sessionId2 = "session-222";
    }

    @Test
    @DisplayName("첫 로그인 시 세션 등록 성공")
    void registerSession_FirstLogin_Success() {
        // when
        String oldSessionId = sessionManagementService.registerSession(testEmail, sessionId1);

        // then
        assertThat(oldSessionId).isNull();  // 기존 세션 없음
        assertThat(sessionManagementService.getActiveSession(testEmail)).isEqualTo(sessionId1);
        assertThat(sessionManagementService.getUserBySession(sessionId1)).isEqualTo(testEmail);
        assertThat(sessionManagementService.isLoggedIn(testEmail)).isTrue();
    }

    @Test
    @DisplayName("중복 로그인 시 기존 세션 무효화")
    void registerSession_DuplicateLogin_InvalidatesOldSession() {
        // given
        sessionManagementService.registerSession(testEmail, sessionId1);

        MapSession oldSession = new MapSession(sessionId1);
        given(sessionRepository.findById(sessionId1)).willReturn(oldSession);

        // when
        String oldSessionId = sessionManagementService.registerSession(testEmail, sessionId2);

        // then
        assertThat(oldSessionId).isEqualTo(sessionId1);  // 기존 세션 ID 반환
        assertThat(sessionManagementService.getActiveSession(testEmail)).isEqualTo(sessionId2);  // 새 세션으로 교체
        assertThat(sessionManagementService.getUserBySession(sessionId1)).isNull();  // 옛날 세션 매핑 제거
        assertThat(sessionManagementService.getUserBySession(sessionId2)).isEqualTo(testEmail);  // 새 세션 매핑

        verify(sessionRepository).deleteById(sessionId1);  // 실제 세션 삭제 확인
    }

    @Test
    @DisplayName("로그아웃 시 세션 제거")
    void removeSession_Success() {
        // given
        sessionManagementService.registerSession(testEmail, sessionId1);

        // when
        sessionManagementService.removeSession(sessionId1);

        // then
        assertThat(sessionManagementService.getActiveSession(testEmail)).isNull();
        assertThat(sessionManagementService.getUserBySession(sessionId1)).isNull();
        assertThat(sessionManagementService.isLoggedIn(testEmail)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 세션 제거 시도 - 에러 없음")
    void removeSession_NonExistentSession_NoError() {
        // when & then
        sessionManagementService.removeSession("non-existent-session");
        // 에러 없이 정상 종료되어야 함
    }

    @Test
    @DisplayName("강제 로그아웃 성공")
    void forceLogout_Success() {
        // given
        sessionManagementService.registerSession(testEmail, sessionId1);

        MapSession session = new MapSession(sessionId1);
        given(sessionRepository.findById(sessionId1)).willReturn(session);

        // when
        boolean result = sessionManagementService.forceLogout(testEmail);

        // then
        assertThat(result).isTrue();
        assertThat(sessionManagementService.isLoggedIn(testEmail)).isFalse();
        verify(sessionRepository).deleteById(sessionId1);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자 강제 로그아웃 시도 - 실패")
    void forceLogout_NotLoggedIn_ReturnsFalse() {
        // when
        boolean result = sessionManagementService.forceLogout(testEmail);

        // then
        assertThat(result).isFalse();
        verify(sessionRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("활성 세션 수 조회")
    void getActiveSessionCount() {
        // given
        sessionManagementService.registerSession("user1@g.seoil.ac.kr", "session-1");
        sessionManagementService.registerSession("user2@g.seoil.ac.kr", "session-2");
        sessionManagementService.registerSession("user3@g.seoil.ac.kr", "session-3");

        // when
        int count = sessionManagementService.getActiveSessionCount();

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("이미 만료된 세션 무효화 시도 - 에러 없음")
    void invalidateSession_ExpiredSession_NoError() {
        // given
        given(sessionRepository.findById(sessionId1)).willReturn(null);  // 세션이 이미 없음

        // when & then
        sessionManagementService.invalidateSession(sessionId1);
        // 에러 없이 정상 종료되어야 함

        verify(sessionRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("모든 세션 정보 초기화")
    void clearAll() {
        // given
        sessionManagementService.registerSession("user1@g.seoil.ac.kr", "session-1");
        sessionManagementService.registerSession("user2@g.seoil.ac.kr", "session-2");

        // when
        sessionManagementService.clearAll();

        // then
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(0);
        assertThat(sessionManagementService.isLoggedIn("user1@g.seoil.ac.kr")).isFalse();
        assertThat(sessionManagementService.isLoggedIn("user2@g.seoil.ac.kr")).isFalse();
    }

    @Test
    @DisplayName("여러 사용자 동시 로그인 관리")
    void multipleUsers_ConcurrentLogin() {
        // given
        String email1 = "user1@g.seoil.ac.kr";
        String email2 = "user2@g.seoil.ac.kr";
        String email3 = "user3@g.seoil.ac.kr";

        // when
        sessionManagementService.registerSession(email1, "session-1");
        sessionManagementService.registerSession(email2, "session-2");
        sessionManagementService.registerSession(email3, "session-3");

        // then
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(3);
        assertThat(sessionManagementService.getActiveSession(email1)).isEqualTo("session-1");
        assertThat(sessionManagementService.getActiveSession(email2)).isEqualTo("session-2");
        assertThat(sessionManagementService.getActiveSession(email3)).isEqualTo("session-3");
    }

    @Test
    @DisplayName("같은 사용자가 3번 연속 로그인 - 마지막 세션만 유지")
    void sameUser_MultipleLogins_OnlyLastSessionActive() {
        // given
        MapSession session1 = new MapSession(sessionId1);
        MapSession session2 = new MapSession(sessionId2);
        String sessionId3 = "session-333";

        given(sessionRepository.findById(sessionId1)).willReturn(session1);
        given(sessionRepository.findById(sessionId2)).willReturn(session2);

        // when
        sessionManagementService.registerSession(testEmail, sessionId1);
        sessionManagementService.registerSession(testEmail, sessionId2);
        sessionManagementService.registerSession(testEmail, sessionId3);

        // then
        assertThat(sessionManagementService.getActiveSessionCount()).isEqualTo(1);
        assertThat(sessionManagementService.getActiveSession(testEmail)).isEqualTo(sessionId3);

        verify(sessionRepository).deleteById(sessionId1);
        verify(sessionRepository).deleteById(sessionId2);
    }
}