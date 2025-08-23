package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    private final SpringDataUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private static final String[] FIRST_NAMES = {
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "전",
        "홍", "고", "문", "양", "손", "배", "조", "백", "허", "유", "남", "심", "노", "정", "하", "곽", "성", "차", "주", "우"
    };

    private static final String[] LAST_NAMES = {
        "민수", "지혜", "준호", "수진", "현우", "미영", "성호", "은정", "태윤", "소영", "동현", "예린", "진우", "하늘", "승민",
        "다은", "우진", "채원", "시우", "서연", "도윤", "지윤", "건우", "유진", "준서", "서현", "민준", "지아", "예준", "서우"
    };

    private static final String[] DOMAINS = {
        "gmail.com", "naver.com", "daum.net", "kakao.com", "yahoo.com", "outlook.com", "hotmail.com"
    };

    public DataController(SpringDataUserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/generate-users")
    public ResponseEntity<?> generateFakeUsers(@RequestParam(defaultValue = "1000") int count) {
        try {
            long startTime = System.currentTimeMillis();
            long existingCountBefore = userRepository.count();

            List<UserEntity> users = new ArrayList<>();
            String encodedPassword = passwordEncoder.encode("password123");
            RoleEntity userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));

            long timestamp = System.currentTimeMillis();
            for (int i = 1; i <= count; i++) {
                long uniqueId = timestamp + existingCountBefore + i;
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                String username = firstName + lastName + "_" + uniqueId;
                String email = "user_" + uniqueId + "@" + DOMAINS[random.nextInt(DOMAINS.length)];

                int attempt = 0;
                while (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
                    attempt++;
                    uniqueId = timestamp + existingCountBefore + i + attempt * 1000000L;
                    username = firstName + lastName + "_" + uniqueId;
                    email = "user_" + uniqueId + "@" + DOMAINS[random.nextInt(DOMAINS.length)];
                    if (attempt > 100) {
                        throw new RuntimeException("중복 해결에 실패했습니다. 시스템 관리자에게 문의하세요.");
                    }
                }

                UserEntity user = new UserEntity();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(encodedPassword);
                user.setRole(userRole);
                users.add(user);
            }

            userRepository.saveAll(users);

            long existingCountAfter = userRepository.count();
            long actuallyCreated = existingCountAfter - existingCountBefore;
            long duration = System.currentTimeMillis() - startTime;

            return ResponseEntity.ok(Map.of(
                "message", actuallyCreated + "명의 새로운 유저가 추가로 생성되었습니다.",
                "requestedCount", count,
                "actuallyCreated", actuallyCreated,
                "totalUsersBefore", existingCountBefore,
                "totalUsersAfter", existingCountAfter,
                "duration", duration + "ms"
            ));
        } catch (RuntimeException e) {
            log.error("Failed to generate fake users", e);
            return ResponseEntity.badRequest().body(Map.of("error", "유저 생성 중 오류: " + e.getMessage()));
        }
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminUser() {
        try {
            if (userRepository.existsByEmail("admin@example.com")) {
                return ResponseEntity.badRequest().body(Map.of("error", "관리자 계정이 이미 존재합니다."));
            }
            RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
            return ResponseEntity.ok(Map.of("message", "관리자 계정이 생성되었습니다."));
        } catch (RuntimeException e) {
            log.error("Failed to create admin", e);
            return ResponseEntity.badRequest().body(Map.of("error", "관리자 계정 생성 오류: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear-users")
    public ResponseEntity<?> clearAllUsers() {
        try {
            long count = userRepository.count();
            userRepository.deleteAll();
            return ResponseEntity.ok(Map.of(
                "message", "모든 유저가 삭제되었습니다.",
                "deletedCount", count
            ));
        } catch (RuntimeException e) {
            log.error("Failed to clear users", e);
            return ResponseEntity.badRequest().body(Map.of("error", "유저 삭제 오류: " + e.getMessage()));
        }
    }
}