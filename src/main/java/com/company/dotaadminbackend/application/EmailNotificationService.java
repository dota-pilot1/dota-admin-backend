package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.model.User;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    private final JavaMailSender mailSender;
    private final SpringDataUserRepository userRepository;
    
    @Value("${notification.email.admin:admin@example.com}")
    private String adminEmail;
    
    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    public EmailNotificationService(JavaMailSender mailSender, SpringDataUserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    public void sendMemberJoinNotification(String newMemberName, String newMemberEmail) {
        if (!emailEnabled) {
            logger.info("이메일 알림이 비활성화되어 있습니다.");
            return;
        }

        List<String> notificationEmails = getNotificationEmails();
        
        if (notificationEmails.isEmpty()) {
            logger.info("알림을 받을 이메일 주소가 없습니다.");
            return;
        }

        String subject = "🎉 새로운 회원이 가입했습니다!";
        String content = createEmailContent(newMemberName, newMemberEmail);
        
        // 관리자에게 발송
        sendEmail(adminEmail, subject, content);
        
        // 알림 동의한 회원들에게 발송
        for (String email : notificationEmails) {
            sendEmail(email, subject, content);
        }
        
        logger.info("{}명에게 회원가입 알림 이메일을 발송했습니다. (관리자 포함)", notificationEmails.size() + 1);
    }

    private List<String> getNotificationEmails() {
        return userRepository.findAll().stream()
                .filter(user -> user.isKakaoNotificationConsent()) // 알림 동의한 회원
                .map(user -> user.getEmail())
                .toList();
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom(adminEmail);
            
            mailSender.send(message);
            logger.debug("이메일 발송 성공: {}", to);
        } catch (Exception e) {
            logger.error("이메일 발송 실패: {} - {}", to, e.getMessage());
        }
    }

    private String createEmailContent(String memberName, String memberEmail) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return String.format("""
                안녕하세요!
                
                새로운 회원이 가입했습니다. 🎉
                
                회원 정보:
                • 이름: %s
                • 이메일: %s
                • 가입시간: %s
                
                관리자 페이지에서 자세한 정보를 확인하실 수 있습니다.
                
                ---
                DOTA Admin Backend
                """, memberName, memberEmail, timestamp);
    }
}