package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.ChallengeRewardService;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.domain.reward.dto.ChallengeRewardResponse;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자별 포상 조회 컨트롤러
 */
@RestController
@RequestMapping("/api/rewards")
public class UserRewardController {
    
    private final ChallengeRewardService challengeRewardService;
    private final UserService userService;
    
    public UserRewardController(ChallengeRewardService challengeRewardService, UserService userService) {
        this.challengeRewardService = challengeRewardService;
        this.userService = userService;
    }
    
    // 현재 사용자의 모든 포상 내역 조회 - GET /api/rewards/my
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyRewards() {
        try {
            UserEntity currentUser = userService.getCurrentUser();
            List<ChallengeRewardResponse> rewards = challengeRewardService.getRewardsByParticipantId(currentUser.getId());
            
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
}
