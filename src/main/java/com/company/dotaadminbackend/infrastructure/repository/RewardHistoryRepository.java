package com.company.dotaadminbackend.infrastructure.repository;

import com.company.dotaadminbackend.domain.reward.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {
    
    /**
     * 특정 챌린지의 포상 지급 이력 조회
     */
    List<RewardHistory> findByChallengeId(Long challengeId);
    
    /**
     * 특정 챌린지에서 포상받은 참가자 ID 목록 조회
     */
    @Query("SELECT rh.participantId FROM RewardHistory rh WHERE rh.challengeId = :challengeId")
    List<Long> findParticipantIdsByChallengeId(@Param("challengeId") Long challengeId);
    
    /**
     * 특정 챌린지에서 특정 참가자가 포상받았는지 확인
     */
    boolean existsByChallengeIdAndParticipantId(Long challengeId, Long participantId);
    
    /**
     * 특정 챌린지에서 특정 참가자의 포상 이력 조회
     */
    Optional<RewardHistory> findByChallengeIdAndParticipantId(Long challengeId, Long participantId);
    
    /**
     * 특정 챌린지의 포상 지급된 참가자 수 조회
     */
    @Query("SELECT COUNT(rh) FROM RewardHistory rh WHERE rh.challengeId = :challengeId")
    Long countByChallengeId(@Param("challengeId") Long challengeId);
}
