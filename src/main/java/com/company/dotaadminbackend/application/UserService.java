package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.model.User;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final SpringDataUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SpringDataUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password, String email) {
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
        entity.setRole("USER");
        
        entity = repository.save(entity);
        return convertToUser(entity);
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

    private User convertToUser(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPassword(), 
                       entity.getEmail(), entity.getRole());
    }
}