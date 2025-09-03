package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.application.SimplePresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimplePresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();
        String attrUserId = null;
        if (headerAccessor.getSessionAttributes() != null) {
            Object v = headerAccessor.getSessionAttributes().get("userId");
            if (v != null) attrUserId = String.valueOf(v);
        }
        
        if (principal != null || attrUserId != null) {
            String userId = principal != null ? principal.getName() : attrUserId;
            log.info("WebSocket connection established for user: {} with session: {}", 
                    userId, sessionId);
            presenceService.onConnect(sessionId, userId);
        } else {
            log.info("Anonymous WebSocket connection established with session: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();
        String attrUserId = null;
        if (headerAccessor.getSessionAttributes() != null) {
            Object v = headerAccessor.getSessionAttributes().get("userId");
            if (v != null) attrUserId = String.valueOf(v);
        }
        
        if (principal != null || attrUserId != null) {
            String userId = principal != null ? principal.getName() : attrUserId;
            log.info("WebSocket connection closed for user: {} with session: {}", 
                    userId, sessionId);
            presenceService.onDisconnect(sessionId);
        } else {
            log.info("Anonymous WebSocket connection closed with session: {}", sessionId);
        }
    }
}
