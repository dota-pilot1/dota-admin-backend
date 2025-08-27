package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class KakaoNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(KakaoNotificationService.class);
    
    private final OkHttpClient httpClient;
    private final SpringDataUserRepository userRepository;
    
    @Value("${kakao.api.key:}")
    private String kakaoApiKey;

    public KakaoNotificationService(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
        this.httpClient = new OkHttpClient();
    }

    public void sendMemberJoinNotification(String newMemberName) {
        if (kakaoApiKey.isEmpty()) {
            logger.warn("카카오톡 API 키가 설정되지 않았습니다. 알림을 보낼 수 없습니다.");
            return;
        }

        List<String> phoneNumbers = getNotificationConsentUsers();
        
        if (phoneNumbers.isEmpty()) {
            logger.info("알림 수신에 동의한 회원이 없습니다.");
            return;
        }

        String message = String.format("🎉 새로운 회원이 가입했습니다!\n회원명: %s", newMemberName);
        
        for (String phoneNumber : phoneNumbers) {
            sendKakaoMessage(phoneNumber, message);
        }
        
        logger.info("{}명의 회원에게 가입 알림을 발송했습니다.", phoneNumbers.size());
    }

    private List<String> getNotificationConsentUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.isKakaoNotificationConsent() && user.getPhoneNumber() != null)
                .map(user -> user.getPhoneNumber())
                .toList();
    }

    private void sendKakaoMessage(String phoneNumber, String message) {
        try {
            // 카카오톡 메시지 API - 나에게 보내기 (테스트용)
            RequestBody requestBody = new FormBody.Builder()
                    .add("template_object", createMessageTemplate(message))
                    .build();

            Request request = new Request.Builder()
                    .url("https://kapi.kakao.com/v2/api/talk/memo/default/send")
                    .addHeader("Authorization", "Bearer " + kakaoApiKey)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("카카오톡 메시지 발송 성공 (나에게 보내기): {}", message);
                } else {
                    logger.error("카카오톡 메시지 발송 실패: {} - {}", response.code(), response.message());
                    if (response.body() != null) {
                        logger.error("응답 내용: {}", response.body().string());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("카카오톡 API 호출 중 오류 발생: {}", e.getMessage());
        }
    }

    private String createMessageTemplate(String message) {
        return String.format("""
                {
                    "object_type": "text",
                    "text": "%s",
                    "link": {
                        "web_url": "https://your-app-url.com",
                        "mobile_web_url": "https://your-app-url.com"
                    }
                }
                """, message);
    }
}