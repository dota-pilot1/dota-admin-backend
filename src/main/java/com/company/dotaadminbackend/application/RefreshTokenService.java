package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.infrastructure.entity.RefreshTokenEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.List;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTtlMs;

    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class GeneratedToken {
        public final String raw;
        public final RefreshTokenEntity entity;
        public GeneratedToken(String raw, RefreshTokenEntity entity) { this.raw = raw; this.entity = entity; }
    }

    @Transactional
    public GeneratedToken create(UserEntity user, String ip, String userAgent) {
        byte[] bytes = new byte[48]; // 64 base64 length approx
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setUser(user);
        e.setTokenHash(passwordEncoder.encode(raw));
        e.setExpiresAt(Instant.now().plusMillis(refreshTtlMs));
        e.setIp(ip);
        e.setUserAgent(userAgent);
        repository.save(e);
        return new GeneratedToken(raw, e);
    }

    public Optional<RefreshTokenEntity> findValid(String raw) {
        List<RefreshTokenEntity> all = repository.findAll();
        return all.stream()
                .filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()))
                .filter(rt -> passwordEncoder.matches(raw, rt.getTokenHash()))
                .findFirst();
    }

    @Transactional
    public void revoke(RefreshTokenEntity token) {
        token.setRevoked(true);
        repository.save(token);
    }
}
