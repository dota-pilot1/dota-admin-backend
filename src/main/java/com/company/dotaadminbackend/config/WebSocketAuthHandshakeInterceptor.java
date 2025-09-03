package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * Handshake 단계에서 JWT(access token) 검증 후 user 식별자를 attributes에 저장.
 * 클라이언트는 ws://host/ws?token=Bearer%20<JWT> 또는 token=<JWT> 형태로 전달.
 */
@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthHandshakeInterceptor.class);
    private final JwtUtil jwtUtil;

    public WebSocketAuthHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            String query = uri.getQuery();
            String tokenParam = null;
            // 1) Query parameter ?token=... 우선
            if (query != null) {
                for (String part : query.split("&")) {
                    if (part.startsWith("token=")) {
                        tokenParam = part.substring("token=".length());
                        break;
                    }
                }
            }
            // 2) Authorization 헤더 (Bearer ...)
            if ((tokenParam == null || tokenParam.isBlank()) && request.getHeaders().containsKey("Authorization")) {
                String auth = request.getHeaders().getFirst("Authorization");
                if (auth != null && !auth.isBlank()) tokenParam = auth;
            }
            if (tokenParam == null || tokenParam.isBlank()) {
                log.warn("WS handshake missing token param");
                return false;
            }
            // URL 인코딩된 'Bearer%20' 제거 허용
            tokenParam = tokenParam.replace("Bearer%20", "").replace("Bearer+", "").replace("Bearer", "").trim();
            if (jwtUtil.isTokenExpired(tokenParam) || !jwtUtil.validateToken(tokenParam)) {
                log.warn("Invalid or expired WS token");
                return false;
            }
            JwtUtil.TokenInfo info = jwtUtil.getTokenInfo(tokenParam);
            attributes.put("userId", info.getEmail());
            attributes.put("role", info.getRole());
            log.debug("WS handshake success user={} role={}", info.getEmail(), info.getRole());
            return true;
        } catch (Exception e) {
            log.error("WS handshake error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, @Nullable Exception exception) {
        // no-op
    }
}
