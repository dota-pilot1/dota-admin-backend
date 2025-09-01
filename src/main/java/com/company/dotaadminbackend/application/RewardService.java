package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.reward.RewardHistory;
import com.company.dotaadminbackend.domain.reward.RewardType;
import com.company.dotaadminbackend.infrastructure.repository.RewardHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RewardService {

    private final RewardHistoryRepository rewardHistoryRepository;

    public RewardService(RewardHistoryRepository rewardHistoryRepository) {
        this.rewardHistoryRepository = rewardHistoryRepository;
    }

    /**
     * 포상 지급 처리
     */
    public RewardHistory giveReward(Long challengeId, Long participantId, Integer amount, RewardType type) {
        // 중복 지급 방지 체크
        if (isAlreadyRewarded(challengeId, participantId)) {
            throw new IllegalArgumentException("이미 포상이 지급된 참가자입니다.");
        }
        
        RewardHistory rewardHistory = new RewardHistory(challengeId, participantId, amount, type);
        return rewardHistoryRepository.save(rewardHistory);
    }

    /**
     * 특정 챌린지의 포상 지급 이력 조회
     */
    @Transactional(readOnly = true)
    public List<RewardHistory> getRewardHistoriesByChallengeId(Long challengeId) {
        return rewardHistoryRepository.findByChallengeId(challengeId);
    }

    /**
     * 특정 챌린지에서 포상받은 참가자 ID 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Long> getRewardedParticipantIds(Long challengeId) {
        System.out.println("🔍 [DEBUG] getRewardedParticipantIds 호출 - challengeId: " + challengeId);
        List<Long> result = rewardHistoryRepository.findParticipantIdsByChallengeId(challengeId);
        System.out.println("📝 [DEBUG] Repository 조회 결과: " + result);
        return result;
    }

    /**
     * 특정 참가자가 이미 포상받았는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isAlreadyRewarded(Long challengeId, Long participantId) {
        return rewardHistoryRepository.existsByChallengeIdAndParticipantId(challengeId, participantId);
    }

    /**
     * 특정 챌린지의 포상 지급된 참가자 수 조회
     */
    @Transactional(readOnly = true)
    public Long getRewardedCount(Long challengeId) {
        System.out.println("🔍 [DEBUG] getRewardedCount 호출 - challengeId: " + challengeId);
        Long result = rewardHistoryRepository.countByChallengeId(challengeId);
        System.out.println("📊 [DEBUG] Count 조회 결과: " + result);
        return result;
    }

    /**
     * 특정 참가자의 포상 이력 조회
     */
    @Transactional(readOnly = true)
    public Optional<RewardHistory> getRewardHistory(Long challengeId, Long participantId) {
        return rewardHistoryRepository.findByChallengeIdAndParticipantId(challengeId, participantId);
    }
}
