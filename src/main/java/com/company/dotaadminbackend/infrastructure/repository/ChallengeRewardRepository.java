package com.company.dotaadminbackend.infrastructure.repository;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRewardRepository extends JpaRepository<ChallengeRewardEntity, Long> {
    
    // 특정 챌린지의 모든 포상 내역 조회
    List<ChallengeRewardEntity> findByChallengeIdOrderByCreatedAtDesc(Long challengeId);
    
    // 특정 참가자의 모든 포상 내역 조회
    List<ChallengeRewardEntity> findByParticipantIdOrderByCreatedAtDesc(Long participantId);
    
    // 특정 챌린지의 특정 참가자 포상 내역 조회
    List<ChallengeRewardEntity> findByChallengeIdAndParticipantIdOrderByCreatedAtDesc(Long challengeId, Long participantId);
    
    // 처리되지 않은 포상 내역 조회
    List<ChallengeRewardEntity> findByProcessedFalseOrderByCreatedAtAsc();
    
    // 특정 관리자가 지급한 포상 내역 조회
    List<ChallengeRewardEntity> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    // 특정 챌린지의 총 포상 금액 합계
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM ChallengeRewardEntity r WHERE r.challengeId = :challengeId AND r.processed = true")
    Integer getTotalRewardAmountByChallengeId(@Param("challengeId") Long challengeId);
    
    // 특정 참가자가 받은 총 포상 금액 합계
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM ChallengeRewardEntity r WHERE r.participantId = :participantId AND r.processed = true")
    Integer getTotalRewardAmountByParticipantId(@Param("participantId") Long participantId);
    
    // 특정 챌린지에서 포상을 받은 참가자 수 조회
    @Query("SELECT COUNT(DISTINCT r.participantId) FROM ChallengeRewardEntity r WHERE r.challengeId = :challengeId AND r.processed = true")
    Long getRewardedParticipantCountByChallengeId(@Param("challengeId") Long challengeId);
}
