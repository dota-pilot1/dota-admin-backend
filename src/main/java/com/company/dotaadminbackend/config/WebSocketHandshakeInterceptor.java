package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            log.info("WebSocket handshake attempt from: {}", request.getRemoteAddress());
            
            // WebSocket 핸드셰이크는 일단 허용하고, STOMP 연결 시에 인증 처리
            log.info("WebSocket handshake successful - authentication will be handled at STOMP level");
            
            return true;
            
        } catch (Exception e) {
            log.error("WebSocket handshake failed due to error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake completed with error: {}", exception.getMessage());
        }
    }
}
