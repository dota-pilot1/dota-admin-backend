package com.company.dotaadminbackend.bootstrap;

import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Application bootstrap task: ensure baseline Roles exist (idempotent).
 * Controlled by property: app.roles.autocreate (default true)
 * Safe to keep enabled in all environments; it only inserts when missing.
 */
@Component
public class RoleInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);

    private final RoleRepository roleRepository;

    @Value("${app.roles.autocreate:true}")
    private boolean autoCreate;

    private static final List<RoleSeed> BASE_ROLES = Arrays.asList(
        new RoleSeed("USER", "기본 사용자"),
        new RoleSeed("ADMIN", "관리자"),
        new RoleSeed("DEVELOPER", "개발자 (임시/이전 구조 호환)")
    );

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!autoCreate) {
            log.info("[RoleInitializer] Skipped (app.roles.autocreate=false)");
            return;
        }
        int created = 0;
        for (RoleSeed seed : BASE_ROLES) {
            if (!roleRepository.existsByName(seed.name())) {
                RoleEntity entity = new RoleEntity();
                entity.setName(seed.name());
                entity.setDescription(seed.description());
                roleRepository.save(entity);
                created++;
                log.info("[RoleInitializer] Created missing role: {}", seed.name());
            }
        }
        log.info("[RoleInitializer] Baseline role check complete. Newly created: {}", created);
    }

    private record RoleSeed(String name, String description) {}
}
