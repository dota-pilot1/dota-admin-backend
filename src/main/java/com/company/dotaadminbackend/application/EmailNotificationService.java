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

        String subject = "🎉 회원가입을 환영합니다!";
        String content = createWelcomeEmailContent(newMemberName);
        
        // 신규 가입자에게 환영 이메일 발송
        sendEmail(newMemberEmail, subject, content);
        
        logger.info("신규 가입자 {}에게 환영 이메일을 발송했습니다.", newMemberEmail);
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

    private String createWelcomeEmailContent(String memberName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return String.format("""
                안녕하세요, %s님!
                
                DOTA에 가입해 주셔서 감사합니다! 🎉
                
                가입 완료:
                • 가입시간: %s
                • 이제 모든 서비스를 이용하실 수 있습니다.
                
                궁금한 점이 있으시면 언제든 문의해 주세요.
                
                감사합니다.
                
                ---
                DOTA Team
                """, memberName, timestamp);
    }
}