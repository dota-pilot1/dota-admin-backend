package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.application.event.MemberJoinEvent;
import com.company.dotaadminbackend.domain.model.User;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final SpringDataUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(SpringDataUserRepository repository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public User register(String username, String password, String email) {
        return register(username, password, email, null, false);
    }

    public User register(String username, String password, String email, String phoneNumber, boolean kakaoNotificationConsent) {
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(password));
        entity.setEmail(email);
        entity.setPhoneNumber(phoneNumber);
        entity.setKakaoNotificationConsent(kakaoNotificationConsent);
        entity.setRole("USER");
        
        entity = repository.save(entity);
        User user = convertToUser(entity);
        
        // 회원가입 이벤트 발행
        eventPublisher.publishEvent(new MemberJoinEvent(this, username, email));
        
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(this::convertToUser);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(this::convertToUser);
    }

    public Optional<User> findByLoginId(String loginId) {
        Optional<UserEntity> userEntity = repository.findByUsername(loginId);
        if (userEntity.isEmpty()) {
            userEntity = repository.findByEmail(loginId);
        }
        return userEntity.map(this::convertToUser);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::convertToUser);
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id)
                .map(this::convertToUser);
    }

    public User updateUser(Long id, String username, String email) {
        UserEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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

        entity = repository.save(entity);
        return convertToUser(entity);
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        repository.deleteById(id);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }

    public User updateCurrentUserProfile(String username, String email) {
        User currentUser = getCurrentUser();
        return updateUser(currentUser.getId(), username, email);
    }

    public boolean isCurrentUser(Long userId) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    private User convertToUser(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPassword(), 
                       entity.getEmail(), entity.getRole(), entity.getPhoneNumber(), 
                       entity.isKakaoNotificationConsent());
    }
}