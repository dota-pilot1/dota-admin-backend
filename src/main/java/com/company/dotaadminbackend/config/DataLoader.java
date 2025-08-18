package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();
    
    @Value("${app.data.load-initial-users:false}")
    private boolean loadInitialUsers;

    private final String[] firstNames = {
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "전",
        "홍", "고", "문", "양", "손", "배", "조", "백", "허", "유", "남", "심", "노", "정", "하", "곽", "성", "차", "주", "우"
    };

    private final String[] lastNames = {
        "민수", "지혜", "준호", "수진", "현우", "미영", "성호", "은정", "태윤", "소영", "동현", "예린", "진우", "하늘", "승민",
        "다은", "우진", "채원", "시우", "서연", "도윤", "지윤", "건우", "유진", "준서", "서현", "민준", "지아", "예준", "서우"
    };

    private final String[] domains = {
        "gmail.com", "naver.com", "daum.net", "kakao.com", "yahoo.com", "outlook.com", "hotmail.com"
    };

    public DataLoader(SpringDataUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!loadInitialUsers) {
            System.out.println("Initial user data loading is disabled.");
            return;
        }
        
        if (userRepository.count() == 0) {
            System.out.println("Loading fake user data...");
            loadFakeUsers();
            System.out.println("Fake user data loaded successfully!");
        } else {
            System.out.println("User data already exists. Skipping data loading.");
        }
    }

    private void loadFakeUsers() {
        List<UserEntity> users = new ArrayList<>();
        
        // 관리자 계정 생성
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        users.add(admin);

        // 1000명의 일반 유저 생성
        for (int i = 1; i <= 1000; i++) {
            UserEntity user = new UserEntity();
            
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String username = firstName + lastName + String.format("%04d", i);
            
            user.setUsername(username);
            user.setEmail(generateEmail(username));
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole("USER");
            
            users.add(user);
        }

        // 배치로 저장 (성능 최적화)
        userRepository.saveAll(users);
    }

    private String generateEmail(String username) {
        String domain = domains[random.nextInt(domains.length)];
        return username.toLowerCase() + "@" + domain;
    }
}