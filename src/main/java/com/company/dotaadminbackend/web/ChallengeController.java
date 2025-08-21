package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.ChallengeService;
import com.company.dotaadminbackend.domain.challenge.Challenge;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import com.company.dotaadminbackend.domain.challenge.dto.CreateChallengeRequest;
import com.company.dotaadminbackend.domain.challenge.dto.ChallengeResponse;
import jakarta.validation.Valid;
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
    public ResponseEntity<Map<String, Object>> createChallenge(@Valid @RequestBody CreateChallengeRequest request) {
        try {
            Challenge challenge = challengeService.createChallenge(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Challenge created successfully");
            response.put("challenge", ChallengeResponse.from(challenge));
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
                response.put("challenge", ChallengeResponse.from(challenge));
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getChallenges(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String authorId) {
        
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
        } else if (authorId != null) {
            try {
                Long parsedAuthorId = Long.valueOf(authorId);
                challenges = challengeService.getChallengesByAuthor(parsedAuthorId);
            } catch (NumberFormatException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid authorId: must be a valid number");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } else {
            challenges = challengeService.getAllChallenges();
        }
        
        List<ChallengeResponse> challengeResponses = challenges.stream()
            .map(ChallengeResponse::from)
            .toList();
            
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("challenges", challengeResponses);
        response.put("count", challenges.size());
        
        return ResponseEntity.ok(response);
    }
}