package com.company.dotaadminbackend.bootstrap;

import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application bootstrap task: ensure a default admin user exists (idempotent).
 * Properties:
 * app.users.autocreate (default true)
 * app.users.admin.username (default admin)
 * app.users.admin.email (default admin@example.com)
 * app.users.admin.password (default admin123)
 */
@Component
public class UserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserInitializer.class);

    private final SpringDataUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.users.autocreate:true}")
    private boolean autoCreate;

    @Value("${app.users.admin.username:admin}")
    private String adminUsername;

    @Value("${app.users.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.users.admin.password:admin123}")
    private String adminPassword;

    public UserInitializer(SpringDataUserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!autoCreate) {
            log.info("[UserInitializer] Skipped (app.users.autocreate=false)");
            return;
        }

        RoleEntity adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole == null) {
            log.warn("[UserInitializer] ADMIN role missing - cannot create admin user yet.");
            return;
        }

        boolean existsByEmail = userRepository.existsByEmail(adminEmail);
        boolean existsByUsername = userRepository.existsByUsername(adminUsername);
        if (!existsByEmail && !existsByUsername) {
            UserEntity admin = new UserEntity();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(adminRole);
            userRepository.save(admin);
            log.info("[UserInitializer] Created default admin user username={} email={}", adminUsername, adminEmail);
        } else {
            log.info("[UserInitializer] Admin already exists (usernameExists={}, emailExists={}) username={} email={}",
                    existsByUsername, existsByEmail, adminUsername, adminEmail);
        }
    }
}
