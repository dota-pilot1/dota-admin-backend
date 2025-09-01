package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.reward.RewardHistory;
import com.company.dotaadminbackend.domain.reward.RewardType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class RewardHistoryService {
    
    public RewardHistory saveRewardHistory(Map<String, Object> rewardData) {
        // 필수 필드 추출
        Long challengeId = extractLong(rewardData.get("challengeId"));
        Long participantId = extractLong(rewardData.get("participantId"));
        Integer rewardAmount = extractInteger(rewardData.get("rewardAmount"));
        String rewardTypeStr = (String) rewardData.get("rewardType");
        
        // 필수 필드 검증
        if (challengeId == null || participantId == null || rewardAmount == null || rewardTypeStr == null) {
            throw new IllegalArgumentException("Required fields are missing: challengeId, participantId, rewardAmount, rewardType");
        }
        
        // RewardType 검증
        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(rewardTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reward type: " + rewardTypeStr);
        }
        
        // 도메인 엔티티 생성
        RewardHistory rewardHistory = new RewardHistory(challengeId, participantId, rewardAmount, rewardType);
        
        // TODO: Repository로 저장 (나중에 구현)
        return rewardHistory;
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
    
    private Long extractLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}