package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.challenge.Challenge;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import com.company.dotaadminbackend.domain.reward.RewardType;
import com.company.dotaadminbackend.infrastructure.adapter.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ChallengeService {
    
    private final ChallengeRepository challengeRepository;
    
    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }
    
    public Challenge createChallenge(Map<String, Object> challengeData) {
        // 필수 필드 추출
        String title = (String) challengeData.get("title");
        String description = (String) challengeData.get("description");
        String author = (String) challengeData.get("author");
        String startDateStr = (String) challengeData.get("startDate");
        String endDateStr = (String) challengeData.get("endDate");
        
        // 필수 필드 검증
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required");
        }
        if (startDateStr == null || endDateStr == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        // 날짜 파싱
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        // 도메인 엔티티 생성
        Challenge challenge = new Challenge(title, description, author, startDate, endDate);
        
        // 선택적 필드들 설정
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) challengeData.get("tags");
        if (tags != null) {
            challenge.setTags(tags);
        }
        
        // 포상 정보 설정
        Integer rewardAmount = extractInteger(challengeData.get("rewardAmount"));
        String rewardTypeStr = (String) challengeData.get("rewardType");
        if (rewardAmount != null && rewardTypeStr != null) {
            try {
                RewardType rewardType = RewardType.valueOf(rewardTypeStr.toUpperCase());
                challenge.updateReward(rewardAmount, rewardType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid reward type: " + rewardTypeStr);
            }
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
    public List<Challenge> getChallengesByAuthor(String author) {
        return challengeRepository.findByAuthorOrderByCreatedAtDesc(author);
    }
    
    private Integer extractInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}