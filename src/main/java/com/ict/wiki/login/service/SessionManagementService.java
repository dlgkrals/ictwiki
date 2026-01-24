package com.ict.wiki.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동시 로그인 방지 서비스
 * SessionRepository를 사용하여 실제 세션 무효화 가능
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagementService {

    private final SessionRepository<? extends Session> sessionRepository;

    // 이메일 -> 세션ID 매핑
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    // 세션ID -> 이메일 매핑 (역방향 조회용)
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * 새 세션 등록
     * 기존 세션이 있으면 실제로 무효화
     *
     * @param email 사용자 이메일
     * @param newSessionId 새 세션 ID
     * @return 기존 세션 ID (없으면 null)
     */
    public String registerSession(String email, String newSessionId) {
        String oldSessionId = userSessionMap.get(email);

        if (oldSessionId != null) {
            log.warn("중복 로그인 감지 - Email: {}, 기존 세션 무효화: {}, 새 세션: {}",
                    email, oldSessionId, newSessionId);

            // ⭐ 실제 세션 무효화!
            invalidateSession(oldSessionId);

            // 기존 매핑 제거
            sessionUserMap.remove(oldSessionId);
        }

        // 새 세션 등록
        userSessionMap.put(email, newSessionId);
        sessionUserMap.put(newSessionId, email);

        log.info("세션 등록 - Email: {}, SessionId: {}", email, newSessionId);

        return oldSessionId;
    }

    /**
     * 세션 제거 (로그아웃 시)
     *
     * @param sessionId 세션 ID
     */
    public void removeSession(String sessionId) {
        String email = sessionUserMap.remove(sessionId);

        if (email != null) {
            userSessionMap.remove(email);
            log.info("세션 제거 - Email: {}, SessionId: {}", email, sessionId);
        }
    }

    /**
     * 실제 세션 무효화
     * SessionRepository를 사용하여 다른 브라우저의 세션도 무효화
     *
     * @param sessionId 무효화할 세션 ID
     */
    public void invalidateSession(String sessionId) {
        try {
            Session session = sessionRepository.findById(sessionId);
            if (session != null) {
                sessionRepository.deleteById(sessionId);
                log.info("세션 무효화 완료 - SessionId: {}", sessionId);
            } else {
                log.debug("세션이 이미 만료됨 - SessionId: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("세션 무효화 실패 - SessionId: {}, Error: {}", sessionId, e.getMessage());
        }
    }

    /**
     * 특정 사용자 강제 로그아웃
     *
     * @param email 사용자 이메일
     * @return 로그아웃 성공 여부
     */
    public boolean forceLogout(String email) {
        String sessionId = userSessionMap.get(email);

        if (sessionId == null) {
            return false;
        }

        // 실제 세션 무효화
        invalidateSession(sessionId);

        // 매핑 제거
        removeSession(sessionId);

        return true;
    }

    /**
     * 특정 사용자의 활성 세션 ID 조회
     */
    public String getActiveSession(String email) {
        return userSessionMap.get(email);
    }

    /**
     * 특정 세션의 사용자 이메일 조회
     */
    public String getUserBySession(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    /**
     * 현재 활성 세션 수 조회
     */
    public int getActiveSessionCount() {
        return userSessionMap.size();
    }

    /**
     * 특정 사용자가 현재 로그인 중인지 확인
     */
    public boolean isLoggedIn(String email) {
        return userSessionMap.containsKey(email);
    }

    /**
     * 모든 세션 정보 초기화 (테스트용)
     */
    public void clearAll() {
        userSessionMap.clear();
        sessionUserMap.clear();
        log.info("모든 세션 정보 초기화");
    }
}