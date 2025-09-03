package com.company.dotaadminbackend.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * WebSocket 세션 이벤트를 SimplePresenceService에 전달
 * (의존성 문제로 임시 비활성화, config/WebSocketEventListener 사용)
 */
@Component
public class WebSocketPresenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPresenceEventListener.class);
    private final SimplePresenceService presenceService;

    public WebSocketPresenceEventListener(SimplePresenceService presenceService) {
        this.presenceService = presenceService;
    }

    // config/WebSocketEventListener에서 처리됨
}
