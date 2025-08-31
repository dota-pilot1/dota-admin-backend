package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.ChallengeRewardService;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.domain.reward.dto.CreateChallengeRewardRequest;
import com.company.dotaadminbackend.domain.reward.dto.ChallengeRewardResponse;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 챌린지별 포상 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/challenges")
public class ChallengeRewardController {
    
    private final ChallengeRewardService challengeRewardService;
    private final UserService userService;
    
    public ChallengeRewardController(ChallengeRewardService challengeRewardService, UserService userService) {
        this.challengeRewardService = challengeRewardService;
        this.userService = userService;
    }
    
    // 특정 챌린지에 포상 지급 - POST /api/challenges/{challengeId}/rewards
    @PostMapping("/{challengeId:[0-9]+}/rewards")
    public ResponseEntity<Map<String, Object>> createReward(
            @PathVariable Long challengeId,
            @Valid @RequestBody CreateChallengeRewardRequest request) {
        
        try {
            UserEntity currentUser = userService.getCurrentUser();
            ChallengeRewardResponse reward = challengeRewardService.createReward(challengeId, request, currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "포상이 성공적으로 지급되었습니다.");
            response.put("reward", reward);
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
            errorResponse.put("message", "포상 지급 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 특정 챌린지의 포상 내역 조회 - GET /api/challenges/{challengeId}/rewards
    @GetMapping("/{challengeId:[0-9]+}/rewards")
    public ResponseEntity<Map<String, Object>> getRewardsByChallengeId(@PathVariable Long challengeId) {
        try {
            List<ChallengeRewardResponse> rewards = challengeRewardService.getRewardsByChallengeId(challengeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rewards", rewards);
            response.put("count", rewards.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "포상 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 특정 포상 상세 조회 - GET /api/challenges/{challengeId}/rewards/{rewardId}
    @GetMapping("/{challengeId:[0-9]+}/rewards/{rewardId:[0-9]+}")
    public ResponseEntity<Map<String, Object>> getRewardById(
            @PathVariable Long challengeId,
            @PathVariable Long rewardId) {
        
        try {
            return challengeRewardService.getRewardById(rewardId)
                    .map(reward -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("reward", reward);
                        response.put("timestamp", LocalDateTime.now());
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "포상 내역을 찾을 수 없습니다.");
                        errorResponse.put("timestamp", LocalDateTime.now());
                        return ResponseEntity.notFound().build();
                    });
                    
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "포상 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}