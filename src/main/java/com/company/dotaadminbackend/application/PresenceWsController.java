package com.company.dotaadminbackend.application;

import org.springframework.stereotype.Controller;

/**
 * TODO: STOMP @MessageMapping 기반의 활동/커넥트 프레임 처리가 필요하면
 * spring-messaging import 문제 해결 후 다시 구현.
 * (현재 PresenceService는 이벤트 리스너 기반 onConnect/onDisconnect 로 동작)
 */
@Controller
public class PresenceWsController { }
