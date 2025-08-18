package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

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

    public DataController(SpringDataUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/generate-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateFakeUsers(@RequestParam(defaultValue = "1000") int count) {
        try {
            System.out.println("Starting user generation for " + count + " users...");
            long startTime = System.currentTimeMillis();
            
            List<UserEntity> users = new ArrayList<>();
            
            // 기존 유저 수 확인
            long existingCount = userRepository.count();
            
            // 미리 비밀번호 암호화 (동일한 비밀번호 재사용)
            String encodedPassword = passwordEncoder.encode("password123");
            
            // count명의 일반 유저 생성
            for (int i = 1; i <= count; i++) {
                UserEntity user = new UserEntity();
                
                // 고유한 ID 기반 생성 (중복 체크 불필요)
                long uniqueId = existingCount + i;
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String lastName = lastNames[random.nextInt(lastNames.length)];
                String username = firstName + lastName + String.format("%06d", uniqueId);
                String email = "user" + uniqueId + "@" + domains[random.nextInt(domains.length)];
                
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(encodedPassword); // 미리 암호화된 비밀번호 재사용
                user.setRole("USER");
                
                users.add(user);
                
                // 진행상황 출력
                if (i % 100 == 0) {
                    System.out.println("Generated " + i + "/" + count + " users...");
                }
            }

            System.out.println("Saving users to database...");
            // 배치로 저장
            userRepository.saveAll(users);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println("User generation completed in " + duration + "ms");
            
            return ResponseEntity.ok(Map.of(
                "message", count + "명의 가짜 유저가 생성되었습니다.",
                "createdCount", count,
                "totalUsers", userRepository.count(),
                "duration", duration + "ms"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "유저 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdminUser() {
        try {
            if (userRepository.existsByEmail("admin@example.com")) {
                return ResponseEntity.badRequest().body(Map.of("error", "관리자 계정이 이미 존재합니다."));
            }
            
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            
            return ResponseEntity.ok(Map.of("message", "관리자 계정이 생성되었습니다."));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "관리자 계정 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearAllUsers() {
        try {
            long count = userRepository.count();
            userRepository.deleteAll();
            
            return ResponseEntity.ok(Map.of(
                "message", "모든 유저가 삭제되었습니다.",
                "deletedCount", count
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "유저 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    private String generateEmail(String username) {
        String domain = domains[random.nextInt(domains.length)];
        return username.toLowerCase() + "@" + domain;
    }
}