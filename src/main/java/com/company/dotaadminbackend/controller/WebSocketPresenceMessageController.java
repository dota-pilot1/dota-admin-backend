package com.company.dotaadminbackend.controller;

import com.company.dotaadminbackend.application.SimplePresenceService;
import com.company.dotaadminbackend.config.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.Map;

@Controller
public class WebSocketPresenceMessageController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPresenceMessageController.class);

    @Autowired
    private SimplePresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 클라이언트가 /app/presence/auth로 JWT 토큰을 보내면 처리
     */
    @MessageMapping("/presence/auth")
    public void handleAuth(String message, Principal principal) {
        try {
            logger.info("Received auth message: {}", message);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(message);
            String token = jsonNode.get("token").asText();
            
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Empty token received");
                return;
            }

            // JWT 검증 및 사용자 정보 추출
            String userId = jwtUtil.getEmailFromToken(token);
            if (userId == null) {
                logger.warn("Invalid token: cannot extract email");
                return;
            }

            logger.info("Auth successful for user: {}", userId);

            // Presence 서비스에 사용자 등록
            String sessionId = principal != null ? principal.getName() : "unknown";
            boolean isNewUser = presenceService.onConnect(sessionId, userId);
            
            if (isNewUser) {
                // 새 사용자가 접속한 경우 모든 클라이언트에게 브로드캐스트
                Map<String, Object> updateMessage = Map.of(
                    "type", "PRESENCE_UPDATE",
                    "action", "joined",
                    "userId", userId,
                    "online", presenceService.getOnlineUsers(),
                    "timestamp", System.currentTimeMillis()
                );
                
                messagingTemplate.convertAndSend("/topic/presence", updateMessage);
                logger.info("Broadcasted join event for user: {}", userId);
            }

        } catch (Exception e) {
            logger.error("Error processing auth message: {}", e.getMessage(), e);
        }
    }

    /**
     * 클라이언트가 /app/presence/activity로 활동 신호를 보내면 처리
     */
    @MessageMapping("/presence/activity")
    public void handleActivity(String message, Principal principal) {
        try {
            String sessionId = principal != null ? principal.getName() : "unknown";
            logger.debug("Activity signal from session: {}", sessionId);
            
            // 활동 신호 처리 (현재는 로그만)
            // 필요시 last activity timestamp 업데이트 등 추가 가능
            
        } catch (Exception e) {
            logger.error("Error processing activity message: {}", e.getMessage(), e);
        }
    }

    /**
     * 클라이언트가 /app/presence/connect로 연결 신호를 보내면 처리
     */
    @MessageMapping("/presence/connect")
    public void handleConnect(String message, Principal principal) {
        try {
            String sessionId = principal != null ? principal.getName() : "unknown";
            logger.info("Connect signal from session: {}", sessionId);
            
            // 연결 신호 처리 (현재는 로그만)
            
        } catch (Exception e) {
            logger.error("Error processing connect message: {}", e.getMessage(), e);
        }
    }
}
