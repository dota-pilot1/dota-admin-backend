package com.company.dotaadminbackend.infrastructure.adapter;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeEntity;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, Long> {
    List<ChallengeEntity> findByStatusOrderByCreatedAtDesc(ChallengeStatus status);
    List<ChallengeEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    List<ChallengeEntity> findAllByOrderByCreatedAtDesc();
}