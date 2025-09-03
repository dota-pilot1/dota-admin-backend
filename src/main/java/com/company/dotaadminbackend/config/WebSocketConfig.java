package com.company.dotaadminbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 메시징 설정 클래스
 * - 실시간 양방향 통신을 위한 WebSocket 설정
 * - STOMP 프로토콜 사용으로 메시지 브로커 패턴 구현
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketHandshakeInterceptor webSocketHandshakeInterceptor) {
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    /**
     * 메시지 브로커 설정
     * - 클라이언트가 구독할 수 있는 destination과 전송1할 destination 정의
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 경로 설정 (서버 -> 클라이언트)
        // /topic: 1:N 브로드캐스트 (모든 구독자에게 전송)
        // /queue: 1:1 개인 메시지 (특정 사용자에게만 전송)
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 서버로 메시지를 보낼 때 사용할 prefix (클라이언트 -> 서버)
        // 예: /app/presence/activity로 메시지 전송
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록
     * - 클라이언트가 WebSocket 연결을 시작할 수 있는 엔드포인트 정의
     * - 프론트가 순수 WebSocket(STOMP) 사용하므로 SockJS 미사용
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws") // WebSocket 연결 엔드포인트: ws://localhost:8080/ws
        .addInterceptors(webSocketHandshakeInterceptor)
        .setAllowedOriginPatterns("*"); // CORS 설정: 모든 도메인 허용
    }
}
