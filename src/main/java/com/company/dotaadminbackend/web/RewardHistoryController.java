package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.RewardHistoryService;
import com.company.dotaadminbackend.domain.reward.RewardHistory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reward-histories")
public class RewardHistoryController {
    
    private final RewardHistoryService rewardHistoryService;
    
    public RewardHistoryController(RewardHistoryService rewardHistoryService) {
        this.rewardHistoryService = rewardHistoryService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveRewardHistory(@RequestBody Map<String, Object> request) {
        try {
            RewardHistory rewardHistory = rewardHistoryService.saveRewardHistory(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reward history saved successfully");
            response.put("challengeId", rewardHistory.getChallengeId());
            response.put("participantId", rewardHistory.getParticipantId());
            response.put("rewardAmount", rewardHistory.getRewardAmount());
            response.put("rewardType", rewardHistory.getRewardType().name());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/challenges/{challengeId}")
    public ResponseEntity<Map<String, Object>> getChallengeRewards(@PathVariable Long challengeId) {
        // TODO: Repository에서 조회 (나중에 구현)
        Map<String, Object> response = new HashMap<>();
        response.put("challengeId", challengeId);
        response.put("message", "Challenge rewards found (mock data)");
        
        return ResponseEntity.ok(response);
    }
}