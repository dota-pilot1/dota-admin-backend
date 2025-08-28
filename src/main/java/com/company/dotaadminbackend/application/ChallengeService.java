package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeEntity;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import com.company.dotaadminbackend.infrastructure.adapter.ChallengeRepository;
import com.company.dotaadminbackend.domain.challenge.dto.CreateChallengeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChallengeService {
    
    private final ChallengeRepository challengeRepository;
    
    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
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
}