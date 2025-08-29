package com.company.dotaadminbackend.config;

import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
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
    private final RoleRepository roleRepository;
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

    public DataLoader(SpringDataUserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 필수 계정들 생성 (항상 실행)
        createEssentialAccounts();
        
        // 추가 테스트 데이터는 설정에 따라 로딩
        if (loadInitialUsers) {
            System.out.println("Loading additional fake user data...");
            loadAdditionalFakeUsers();
            System.out.println("Additional fake user data loaded successfully!");
        } else {
            System.out.println("Additional user data loading is disabled.");
        }
    }

    private void createEssentialAccounts() {
        System.out.println("Checking and creating essential accounts...");
    // 새 스키마(create) 첫 기동 시 RoleInitializer 보다 먼저 실행될 수 있으므로 방어적으로 ROLE 존재 보장
    RoleEntity adminRole = ensureRole("ADMIN", "관리자");
    RoleEntity userRole = ensureRole("USER", "기본 사용자");

        // 필수 계정들 정의
        String[][] essentialAccounts = {
            {"terecal", "terecal@daum.net", "123456", "ADMIN"}, // 최초 계정은 관리자 권한 부여
            {"test1", "test1@daum.net", "123456", "USER"},
            {"test2", "test2@daum.net", "123456", "USER"}
        };

        for (String[] accountInfo : essentialAccounts) {
            String username = accountInfo[0];
            String email = accountInfo[1];
            String password = accountInfo[2];
            String roleName = accountInfo[3];

            // 이메일로 계정 존재 여부 확인
            userRepository.findByEmail(email).ifPresentOrElse(existing -> {
                System.out.println("Essential account already exists: " + email);
            }, () -> {
                UserEntity user = new UserEntity();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(roleName.equals("ADMIN") ? adminRole : userRole);
                userRepository.save(user);
                System.out.println("Created essential account: " + email);
            });
        }
    }

    /**
     * 지정한 이름의 Role 을 찾아보고 없으면 즉시 생성 (create ddl-auto 상황 초기 부트스트랩 안정성 확보)
     */
    private RoleEntity ensureRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            System.out.println("[DataLoader] Missing role '" + name + "' -> creating inline (race-safe)");
            RoleEntity r = new RoleEntity();
            r.setName(name);
            r.setDescription(description);
            return roleRepository.save(r);
        });
    }

    private void loadAdditionalFakeUsers() {
        if (userRepository.count() > 3) { // 필수 계정 3개 이상이면 추가 데이터 로딩 안함
            System.out.println("Additional user data already exists. Skipping additional data loading.");
            return;
        }
        
        List<UserEntity> users = new ArrayList<>();
        
        // Role 조회
        RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        RoleEntity userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        // 관리자 계정 생성 (이미 없는 경우에만)
        if (!userRepository.existsByEmail("admin@example.com")) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            users.add(admin);
        }

        // 1000명의 일반 유저 생성
        for (int i = 1; i <= 1000; i++) {
            UserEntity user = new UserEntity();
            
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String username = firstName + lastName + String.format("%04d", i);
            
            user.setUsername(username);
            user.setEmail(generateEmail(username));
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(userRole);
            
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