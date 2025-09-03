package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.application.SimplePresenceService;
import com.company.dotaadminbackend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimplePresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("=== SessionConnectEvent triggered ===");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("Session ID: {}", sessionId);
        log.info("All native headers: {}", headerAccessor.toNativeHeaderMap());
        
        // STOMP Connect 헤더에서 Authorization 헤더 추출
        String authHeader = headerAccessor.getFirstNativeHeader("Authorization");
        log.info("Authorization header from STOMP: {}", authHeader);
        
        String userId = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7); // "Bearer " 제거
                log.info("Extracted token from STOMP header: {}", token.substring(0, Math.min(20, token.length())) + "...");
                
                // JWT 토큰 검증 및 사용자 정보 추출
                JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
                userId = tokenInfo.getEmail();
                
                // 세션에 사용자 ID 저장
                headerAccessor.getSessionAttributes().put("userId", userId);
                
                log.info("STOMP authentication successful for user: {}", userId);
                
            } catch (Exception e) {
                log.error("STOMP authentication failed: {}", e.getMessage());
                return; // 인증 실패시 처리 중단
            }
        } else {
            log.warn("No Authorization header in STOMP connection. Available headers: {}", headerAccessor.toNativeHeaderMap());
            return; // 인증 헤더 없으면 처리 중단
        }
        
        if (userId != null) {
            log.info("WebSocket connection established for user: {} with session: {}", userId, sessionId);
            
            // 사용자 온라인 등록
            boolean wasNewUser = presenceService.onConnect(sessionId, userId);
            
            // 실시간 브로드캐스트 (새로운 사용자인 경우에만)
            if (wasNewUser) {
                broadcastPresenceUpdate("joined", userId);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket connection closed for session: {}", sessionId);
        
        // 사용자 오프라인 처리
        String offlineUserId = presenceService.onDisconnect(sessionId);
        
        // 실시간 브로드캐스트 (사용자가 완전히 오프라인된 경우)
        if (offlineUserId != null) {
            broadcastPresenceUpdate("left", offlineUserId);
        }
    }
    
    private void broadcastPresenceUpdate(String action, String userId) {
        Map<String, Object> payload = Map.of(
            "type", "PRESENCE_UPDATE",
            "action", action,
            "userId", userId,
            "online", presenceService.getOnlineUsers(),
            "timestamp", System.currentTimeMillis()
        );
        
        log.debug("Broadcasting presence update: {} for user: {}", action, userId);
        messagingTemplate.convertAndSend("/topic/presence", payload);
    }
}
