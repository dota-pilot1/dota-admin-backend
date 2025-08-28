package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.application.event.MemberJoinEvent;
import com.company.dotaadminbackend.infrastructure.entity.AuthorityEntity;
import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.AuthorityRepository;
import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserService {

    private final SpringDataUserRepository repository;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${app.registration.first-user-admin:true}")
    private boolean firstUserAdmin;

    public UserService(SpringDataUserRepository repository, RoleRepository roleRepository,
            AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public UserEntity register(String username, String password, String email) {
        return register(username, password, email, null, false);
    }

    public UserEntity register(String username, String password, String email, String phoneNumber,
            boolean kakaoNotificationConsent) {
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Decide which role to assign
        final String resolvedRoleName = resolveRegistrationRole();
        RoleEntity userRole = roleRepository.findByName(resolvedRoleName)
                .orElseGet(() -> roleRepository.findByName("DEVELOPER")
                        .orElseThrow(() -> new IllegalArgumentException(resolvedRoleName + " role not found")));

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(password));
        entity.setEmail(email);
        entity.setPhoneNumber(phoneNumber);
        entity.setKakaoNotificationConsent(kakaoNotificationConsent);
        entity.setRole(userRole);

        entity = repository.save(entity);

        // 회원가입 이벤트 발행
        eventPublisher.publishEvent(new MemberJoinEvent(this, username, email));

        return entity;
    }

    public UserEntity registerAdmin(String username, String password, String email) {
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalArgumentException("ADMIN role not found"));

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(password));
        entity.setEmail(email);
        entity.setRole(adminRole);

        return repository.save(entity);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return repository.findByUsername(username)
;
    }

    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmail(email)
;
    }

    public Optional<UserEntity> findByLoginId(String loginId) {
        Optional<UserEntity> userEntity = repository.findByUsername(loginId);
        if (userEntity.isEmpty()) {
            userEntity = repository.findByEmail(loginId);
        }
        return userEntity;
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Page<UserEntity> findAllUserEntitys(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<UserEntity> findUserEntitysByRole(String roleName, Pageable pageable) {
        RoleEntity roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        return repository.findByRole(roleEntity, pageable);
    }

    public Long countUserEntitysByRole(String roleName) {
        return repository.countByRoleName(roleName);
    }

    public List<AuthorityEntity> getUserAuthorities(Long userId) {
        List<AuthorityEntity> authorities = authorityRepository.findAllUserAuthorities(userId);
        return authorities.stream()
                .toList();
    }

    public boolean hasAuthority(Long userId, String authorityName) {
        return getUserAuthorities(userId).stream()
                .anyMatch(auth -> auth.getName().equals(authorityName));
    }

    public Optional<UserEntity> findById(Long id) {
        return repository.findById(id)
;
    }

    public UserEntity updateUserEntity(Long id, String username, String email) {
        UserEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserEntity not found"));

        if (username != null && !username.equals(entity.getUsername())) {
            if (repository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already exists");
            }
            entity.setUsername(username);
        }

        if (email != null && !email.equals(entity.getEmail())) {
            if (repository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            entity.setEmail(email);
        }

        return repository.save(entity);
    }

    public void deleteUserEntity(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("UserEntity not found");
        }
        repository.deleteById(id);
    }

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authentication found");
        }

        String email = authentication.getName();
        if (email == null || email.equals("anonymousUserEntity")) {
            throw new SecurityException("No authenticated user email found");
        }

        return findByEmail(email)
                .orElseThrow(() -> new SecurityException("Current user not found: " + email));
    }

    public UserEntity updateCurrentUserEntityProfile(String username, String email) {
        UserEntity currentUserEntity = getCurrentUser();
        return updateUserEntity(currentUserEntity.getId(), username, email);
    }

    public void deleteCurrentUserEntity(String password) {
        UserEntity currentUserEntity = getCurrentUser();

        // 비밀번호 검증
        UserEntity entity = repository.findById(currentUserEntity.getId())
                .orElseThrow(() -> new IllegalArgumentException("UserEntity not found"));

        if (!validatePassword(password, entity.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // 사용자 삭제
        repository.deleteById(currentUserEntity.getId());
    }

    public boolean isCurrentUserEntity(Long userId) {
        try {
            UserEntity currentUserEntity = getCurrentUser();
            return currentUserEntity.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }


    // Decide which role to assign for a new registration
    private String resolveRegistrationRole() {
        String base = "USER"; // default
        if (!firstUserAdmin) {
            return base;
        }
        try {
            long total = repository.count();
            Long adminCountObj = repository.countByRoleName("ADMIN");
            long adminCount = adminCountObj == null ? 0 : adminCountObj;
            if (total == 0 || adminCount == 0) {
                return "ADMIN"; // first user or no admin present
            }
        } catch (Exception ignored) {
        }
        return base;
    }
}