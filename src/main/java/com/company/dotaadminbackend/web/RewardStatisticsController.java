package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.infrastructure.repository.ChallengeRewardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardStatisticsController {
    
    private final ChallengeRewardRepository challengeRewardRepository;
    
    public RewardStatisticsController(ChallengeRewardRepository challengeRewardRepository) {
        this.challengeRewardRepository = challengeRewardRepository;
    }
    
    // 포상 통계 API - GET /api/rewards/statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRewardStatistics() {
        try {
            // 전체 포상 개수
            long totalRewardsCount = challengeRewardRepository.count();
            
            // 처리된 포상 개수
            long processedRewardsCount = challengeRewardRepository.countByProcessedTrue();
            
            // 총 포상 금액
            Integer totalAmount = challengeRewardRepository.getTotalRewardAmount();
            
            // 챌린지별 포상 통계 (상위 5개)
            List<Map<String, Object>> topChallenges = challengeRewardRepository.getTopChallengesByRewardAmount()
                .stream()
                .limit(5)
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("challengeId", result[0]);
                    item.put("challengeTitle", result[1]);
                    item.put("totalAmount", result[2]);
                    item.put("rewardCount", result[3]);
                    return item;
                })
                .toList();
            
            // 참가자별 포상 통계 (상위 5개)
            List<Map<String, Object>> topParticipants = challengeRewardRepository.getTopParticipantsByRewardAmount()
                .stream()
                .limit(5)
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("participantId", result[0]);
                    item.put("participantName", result[1]);
                    item.put("totalAmount", result[2]);
                    item.put("rewardCount", result[3]);
                    return item;
                })
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "totalRewardsCount", totalRewardsCount,
                "processedRewardsCount", processedRewardsCount,
                "totalAmount", totalAmount != null ? totalAmount : 0,
                "topChallenges", topChallenges,
                "topParticipants", topParticipants
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "포상 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
