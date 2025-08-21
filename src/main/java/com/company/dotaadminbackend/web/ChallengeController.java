package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.ChallengeService;
import com.company.dotaadminbackend.domain.challenge.Challenge;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    
    private final ChallengeService challengeService;
    
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createChallenge(@RequestBody Map<String, Object> request) {
        try {
            Challenge challenge = challengeService.createChallenge(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Challenge created successfully");
            response.put("id", challenge.getId());
            response.put("title", challenge.getTitle());
            response.put("author", challenge.getAuthor());
            response.put("status", challenge.getStatus().name());
            response.put("startDate", challenge.getStartDate());
            response.put("endDate", challenge.getEndDate());
            response.put("rewardAmount", challenge.getRewardAmount());
            response.put("rewardType", challenge.getRewardType() != null ? challenge.getRewardType().name() : null);
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
    
    @GetMapping("/{challengeId}")
    public ResponseEntity<Map<String, Object>> getChallenge(@PathVariable Long challengeId) {
        return challengeService.getChallengeById(challengeId)
            .map(challenge -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("challenge", buildChallengeResponse(challenge));
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getChallenges(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String author) {
        
        List<Challenge> challenges;
        
        if (status != null) {
            try {
                ChallengeStatus challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
                challenges = challengeService.getChallengesByStatus(challengeStatus);
            } catch (IllegalArgumentException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid status: " + status);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } else if (author != null) {
            challenges = challengeService.getChallengesByAuthor(author);
        } else {
            challenges = challengeService.getAllChallenges();
        }
        
        List<Map<String, Object>> challengeResponses = challenges.stream()
            .map(this::buildChallengeResponse)
            .toList();
            
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("challenges", challengeResponses);
        response.put("count", challenges.size());
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> buildChallengeResponse(Challenge challenge) {
        Map<String, Object> challengeData = new HashMap<>();
        challengeData.put("id", challenge.getId());
        challengeData.put("title", challenge.getTitle());
        challengeData.put("description", challenge.getDescription());
        challengeData.put("author", challenge.getAuthor());
        challengeData.put("tags", challenge.getTags());
        challengeData.put("participants", challenge.getParticipants());
        challengeData.put("status", challenge.getStatus().name());
        challengeData.put("startDate", challenge.getStartDate());
        challengeData.put("endDate", challenge.getEndDate());
        challengeData.put("rewardAmount", challenge.getRewardAmount());
        challengeData.put("rewardType", challenge.getRewardType() != null ? challenge.getRewardType().name() : null);
        challengeData.put("createdAt", challenge.getCreatedAt());
        challengeData.put("updatedAt", challenge.getUpdatedAt());
        return challengeData;
    }
}