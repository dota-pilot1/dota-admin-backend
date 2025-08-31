package com.company.dotaadminbackend.infrastructure.repository;

import com.company.dotaadminbackend.infrastructure.entity.RefreshTokenEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    long deleteByUser(UserEntity user);
    void deleteAllByExpiresAtBefore(Instant now);
}
