package com.company.dotaadminbackend.infrastructure.adapter;

import com.company.dotaadminbackend.domain.challenge.Challenge;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByStatusOrderByCreatedAtDesc(ChallengeStatus status);
    List<Challenge> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    List<Challenge> findAllByOrderByCreatedAtDesc();
}