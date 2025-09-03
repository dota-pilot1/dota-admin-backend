package com.company.dotaadminbackend.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 간단한 인메모리 개발자 접속 상태 관리
 */
@Service
@Slf4j
public class SimplePresenceService {

    // sessionId -> userId 매핑
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    
    // 온라인 사용자 목록
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    /**
     * 사용자 연결 처리
     */
    public void onConnect(String sessionId, String userId) {
        sessionUserMap.put(sessionId, userId);
        boolean wasAdded = onlineUsers.add(userId);
        
        if (wasAdded) {
            log.info("User online: {}", userId);
        } else {
            log.debug("User session added but already online: {}", userId);
        }
    }

    /**
     * 사용자 연결 해제 처리
     */
    public void onDisconnect(String sessionId) {
        String userId = sessionUserMap.remove(sessionId);
        if (userId == null) return;

        // 해당 사용자의 다른 세션이 남아있는지 확인
        boolean stillHasSession = sessionUserMap.containsValue(userId);
        
        if (!stillHasSession) {
            // 모든 세션이 종료된 경우 오프라인 처리
            boolean wasRemoved = onlineUsers.remove(userId);
            if (wasRemoved) {
                log.info("User offline: {}", userId);
            }
        }
    }

    /**
     * 온라인 사용자 목록 조회
     */
    public Set<String> getOnlineUsers() {
        return new HashSet<>(onlineUsers);
    }

    /**
     * 사용자 활동 상태 업데이트 (heartbeat)
     */
    public void updateUserActivity(String userId) {
        // 간단 버전에서는 로그만
        log.debug("User activity: {}", userId);
    }
    
    /**
     * 모든 사용자 정리 (테스트용)
     */
    public void clearAll() {
        sessionUserMap.clear();
        onlineUsers.clear();
        log.info("All presence data cleared");
    }
}
