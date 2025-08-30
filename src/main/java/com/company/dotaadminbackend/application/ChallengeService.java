package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import com.company.dotaadminbackend.infrastructure.adapter.ChallengeRepository;
import com.company.dotaadminbackend.infrastructure.adapter.SpringDataUserRepository;
import com.company.dotaadminbackend.infrastructure.repository.ChallengeRewardRepository;
import com.company.dotaadminbackend.domain.challenge.dto.CreateChallengeRequest;
import com.company.dotaadminbackend.domain.challenge.dto.UpdateChallengeRequest;
import com.company.dotaadminbackend.domain.challenge.dto.ChallengeResponse;
import com.company.dotaadminbackend.domain.challenge.dto.ParticipantResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChallengeService {
    
    private final ChallengeRepository challengeRepository;
    private final SpringDataUserRepository userRepository;
    private final ChallengeRewardRepository challengeRewardRepository;
    
    public ChallengeService(ChallengeRepository challengeRepository, SpringDataUserRepository userRepository, ChallengeRewardRepository challengeRewardRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.challengeRewardRepository = challengeRewardRepository;
    }
    
    public ChallengeEntity createChallenge(CreateChallengeRequest request, Long authorId) {
        // 도메인 엔티티 생성
        ChallengeEntity challenge = new ChallengeEntity(
            request.getTitle(), 
            request.getDescription(), 
            authorId, 
            request.getStartDate(), 
            request.getEndDate()
        );
        
        // 선택적 필드들 설정
        if (request.getTags() != null) {
            challenge.setTags(request.getTags());
        }
        
        // 포상 정보 설정
        if (request.getRewardAmount() != null && request.getRewardType() != null) {
            challenge.updateReward(request.getRewardAmount(), request.getRewardType());
        }
        
        // 실제 저장
        return challengeRepository.save(challenge);
    }
    
    @Transactional(readOnly = true)
    public List<ChallengeEntity> getAllChallenges() {
        return challengeRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public Optional<ChallengeEntity> getChallengeById(Long id) {
        return challengeRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<ChallengeEntity> getChallengesByStatus(ChallengeStatus status) {
        return challengeRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Transactional(readOnly = true)
    public List<ChallengeEntity> getChallengesByAuthor(Long authorId) {
        return challengeRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }
    
    // Participation Methods
    public ChallengeEntity participateInChallenge(Long challengeId, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        challenge.addParticipant(userId);
        return challengeRepository.save(challenge);
    }
    
    public ChallengeEntity leaveChallenge(Long challengeId, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        challenge.removeParticipant(userId);
        return challengeRepository.save(challenge);
    }
    
    @Transactional(readOnly = true)
    public boolean isParticipant(Long challengeId, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        return challenge.isParticipant(userId);
    }
    
    // Helper method to convert ChallengeEntity to ChallengeResponse with participant details
    @Transactional(readOnly = true)
    public ChallengeResponse toChallengeResponse(ChallengeEntity challenge) {
        ChallengeResponse response = ChallengeResponse.from(challenge);
        
        // Fetch author name and email
        Optional<UserEntity> author = userRepository.findById(challenge.getAuthorId());
        if (author.isPresent()) {
            UserEntity authorUser = author.get();
            response.setUsername(authorUser.getUsername());
            response.setEmail(authorUser.getEmail());
        }
        
        // Fetch participant details
        List<ParticipantResponse> participants = challenge.getParticipantIds().stream()
                .map(participantId -> {
                    Optional<UserEntity> user = userRepository.findById(participantId);
                    return user.map(ParticipantResponse::from).orElse(null);
                })
                .filter(participant -> participant != null)
                .collect(Collectors.toList());
        
        response.setParticipants(participants);
        
        // Fetch rewarded participant count
        Long rewardedCount = challengeRewardRepository.getRewardedParticipantCountByChallengeId(challenge.getId());
        response.setRewardedParticipantCount(rewardedCount);
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<ChallengeResponse> getAllChallengesWithParticipants() {
        List<ChallengeEntity> challenges = challengeRepository.findAllByOrderByCreatedAtDesc();
        return challenges.stream()
                .map(this::toChallengeResponse)
                .collect(Collectors.toList());
    }
    
    // 상태 변경 메서드들
    public ChallengeResponse startChallenge(Long challengeId, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        // 작성자만 상태 변경 가능
        if (!challenge.getAuthorId().equals(userId)) {
            throw new IllegalStateException("챌린지 작성자만 상태를 변경할 수 있습니다.");
        }
        
        challenge.startChallenge();
        ChallengeEntity updatedChallenge = challengeRepository.save(challenge);
        return toChallengeResponse(updatedChallenge);
    }
    
    public ChallengeResponse completeChallenge(Long challengeId, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        if (!challenge.getAuthorId().equals(userId)) {
            throw new IllegalStateException("챌린지 작성자만 상태를 변경할 수 있습니다.");
        }
        
        challenge.completeChallenge();
        ChallengeEntity updatedChallenge = challengeRepository.save(challenge);
        return toChallengeResponse(updatedChallenge);
    }
    
    public ChallengeResponse reopenChallenge(Long challengeId, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        if (!challenge.getAuthorId().equals(userId)) {
            throw new IllegalStateException("챌린지 작성자만 상태를 변경할 수 있습니다.");
        }
        
        challenge.reopenChallenge();
        ChallengeEntity updatedChallenge = challengeRepository.save(challenge);
        return toChallengeResponse(updatedChallenge);
    }
    
    public ChallengeResponse updateChallenge(Long challengeId, UpdateChallengeRequest request, Long userId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        if (!challenge.getAuthorId().equals(userId)) {
            throw new IllegalStateException("챌린지 작성자만 수정할 수 있습니다.");
        }
        
        // 기본 정보 수정 (엔티티의 updateChallengeInfo 메서드 사용)
        challenge.updateChallengeInfo(
            request.getTitle(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        // 태그 수정
        if (request.getTags() != null) {
            challenge.setTags(request.getTags());
        }
        
        // 보상 정보 수정
        if (request.getRewardAmount() != null && request.getRewardType() != null) {
            challenge.updateReward(request.getRewardAmount(), request.getRewardType());
        }
        
        ChallengeEntity updatedChallenge = challengeRepository.save(challenge);
        return toChallengeResponse(updatedChallenge);
    }
}