package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.reward.dto.CreateChallengeRewardRequest;
import com.company.dotaadminbackend.domain.reward.dto.ChallengeRewardResponse;
import com.company.dotaadminbackend.infrastructure.entity.ChallengeRewardEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.infrastructure.entity.ChallengeEntity;
import com.company.dotaadminbackend.infrastructure.repository.ChallengeRewardRepository;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import com.company.dotaadminbackend.infrastructure.adapter.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChallengeRewardService {
    
    private final ChallengeRewardRepository challengeRewardRepository;
    private final SpringDataUserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    
    public ChallengeRewardService(ChallengeRewardRepository challengeRewardRepository, 
                        SpringDataUserRepository userRepository,
                        ChallengeRepository challengeRepository) {
        this.challengeRewardRepository = challengeRewardRepository;
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
    }
    
    // 포상 지급
    public ChallengeRewardResponse createReward(Long challengeId, CreateChallengeRewardRequest request, Long createdBy) {
        // 챌린지 존재 확인
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));
        
        // 참가자 존재 확인
        UserEntity participant = userRepository.findById(request.getParticipantId())
            .orElseThrow(() -> new IllegalArgumentException("참가자를 찾을 수 없습니다: " + request.getParticipantId()));
        
        // 참가자가 해당 챌린지에 참여했는지 확인
        if (!challenge.getParticipantIds().contains(request.getParticipantId())) {
            throw new IllegalArgumentException("해당 참가자는 이 챌린지에 참여하지 않았습니다.");
        }
        
        // 포상 엔티티 생성
        ChallengeRewardEntity reward = new ChallengeRewardEntity(
            challengeId,
            request.getParticipantId(),
            request.getAmount(),
            request.getMethod(),
            request.getReason(),
            createdBy
        );
        
        // 포상 저장
        ChallengeRewardEntity savedReward = challengeRewardRepository.save(reward);
        
        // 포상 처리 (포인트 지급 등)
        processReward(savedReward);
        
        // 응답 생성 (이름 등 추가 정보 포함)
        return toChallengeRewardResponse(savedReward);
    }
    
    // 포상 처리 (실제 포인트 지급 등)
    private void processReward(ChallengeRewardEntity reward) {
        try {
            switch (reward.getMethod()) {
                case CASH:
                    // 현금 지급 로직 (추후 구현)
                    // paymentService.processCashReward(reward);
                    break;
                case ITEM:
                    // 아이템 지급 로직 (추후 구현)
                    // itemService.processItemReward(reward);
                    break;
            }
            // 처리 완료 표시
            reward.markAsProcessed();
            challengeRewardRepository.save(reward);
        } catch (Exception e) {
            throw new RuntimeException("포상 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 특정 챌린지의 모든 포상 내역 조회
    @Transactional(readOnly = true)
    public List<ChallengeRewardResponse> getRewardsByChallengeId(Long challengeId) {
        List<ChallengeRewardEntity> rewards = challengeRewardRepository.findByChallengeIdOrderByCreatedAtDesc(challengeId);
        return rewards.stream()
                .map(this::toChallengeRewardResponse)
                .toList();
    }
    
    // 특정 참가자의 모든 포상 내역 조회
    @Transactional(readOnly = true)
    public List<ChallengeRewardResponse> getRewardsByParticipantId(Long participantId) {
        List<ChallengeRewardEntity> rewards = challengeRewardRepository.findByParticipantIdOrderByCreatedAtDesc(participantId);
        return rewards.stream()
                .map(this::toChallengeRewardResponse)
                .toList();
    }
    
    // 포상 상세 조회
    @Transactional(readOnly = true)
    public Optional<ChallengeRewardResponse> getRewardById(Long rewardId) {
        return challengeRewardRepository.findById(rewardId)
                .map(this::toChallengeRewardResponse);
    }
    
    // Entity를 Response로 변환 (이름 등 추가 정보 포함)
    private ChallengeRewardResponse toChallengeRewardResponse(ChallengeRewardEntity entity) {
        ChallengeRewardResponse response = ChallengeRewardResponse.from(entity);
        
        // 참가자 이름 추가
        userRepository.findById(entity.getParticipantId())
                .ifPresent(user -> response.setParticipantName(user.getUsername()));
        
        // 지급자 이름 추가
        userRepository.findById(entity.getCreatedBy())
                .ifPresent(user -> response.setCreatedByName(user.getUsername()));
        
        return response;
    }
}
