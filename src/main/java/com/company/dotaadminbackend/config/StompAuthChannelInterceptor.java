package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP 메시지 인터셉터 - JWT 인증 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("STOMP CONNECT message received");
            
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7); // "Bearer " 제거
                    log.info("Extracted token from STOMP header: {}", token.substring(0, Math.min(20, token.length())) + "...");
                    
                    // JWT 토큰 검증 및 사용자 정보 추출
                    JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
                    String userId = tokenInfo.getEmail();
                    
                    if (userId != null && !userId.trim().isEmpty()) {
                        // STOMP 세션에 사용자 ID 저장
                        accessor.getSessionAttributes().put("userId", userId);
                        log.info("STOMP authentication successful for user: {}", userId);
                    } else {
                        log.warn("STOMP authentication failed: No valid user ID in token");
                        return null; // 연결 거부
                    }
                    
                } catch (Exception e) {
                    log.error("STOMP authentication failed due to error: {}", e.getMessage());
                    return null; // 연결 거부
                }
            } else {
                log.warn("STOMP authentication failed: No valid Authorization header");
                return null; // 연결 거부
            }
        }
        
        return message;
    }
}
