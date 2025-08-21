package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.challenge.Challenge;
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
    
    public Challenge createChallenge(CreateChallengeRequest request) {
        // 도메인 엔티티 생성
        Challenge challenge = new Challenge(
            request.getTitle(), 
            request.getDescription(), 
            request.getAuthorId(), 
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
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public Optional<Challenge> getChallengeById(Long id) {
        return challengeRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Challenge> getChallengesByStatus(ChallengeStatus status) {
        return challengeRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Transactional(readOnly = true)
    public List<Challenge> getChallengesByAuthor(Long authorId) {
        return challengeRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }
}