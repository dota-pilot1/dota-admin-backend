package com.company.dotaadminbackend.application.event;

import com.company.dotaadminbackend.application.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MemberJoinEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MemberJoinEventListener.class);
    
    private final EmailNotificationService emailNotificationService;

    public MemberJoinEventListener(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Async
    @EventListener
    public void handleMemberJoinEvent(MemberJoinEvent event) {
        logger.info("회원 가입 이벤트 수신: {} ({})", event.getMemberName(), event.getEmail());
        
        try {
            emailNotificationService.sendMemberJoinNotification(event.getMemberName(), event.getEmail());
        } catch (Exception e) {
            logger.error("이메일 알림 발송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}