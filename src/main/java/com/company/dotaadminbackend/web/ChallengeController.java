package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.ChallengeService;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.infrastructure.entity.ChallengeEntity;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import com.company.dotaadminbackend.domain.challenge.dto.CreateChallengeRequest;
import com.company.dotaadminbackend.domain.challenge.dto.ChallengeResponse;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
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
    private final UserService userService;

    public ChallengeController(ChallengeService challengeService, UserService userService) {
        this.challengeService = challengeService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createChallenge(@Valid @RequestBody CreateChallengeRequest request) {
        UserEntity currentUserEntity = userService.getCurrentUser();
        ChallengeEntity challenge = challengeService.createChallenge(request, currentUserEntity.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "ChallengeEntity created successfully");
        response.put("challenge", ChallengeResponse.from(challenge));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<Map<String, Object>> getChallengeEntity(@PathVariable Long challengeId) {
        ChallengeEntity challenge = challengeService.getChallengeById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("ChallengeEntity not found with id: " + challengeId));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("challenge", ChallengeResponse.from(challenge));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getChallengeEntitys(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String authorId) {

        List<ChallengeEntity> challenges;

        if (status != null) {
            try {
                ChallengeStatus challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
                challenges = challengeService.getChallengesByStatus(challengeStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        } else if (authorId != null) {
            try {
                Long parsedAuthorId = Long.valueOf(authorId);
                challenges = challengeService.getChallengesByAuthor(parsedAuthorId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid authorId: must be a valid number");
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

    // Participation APIs
    @PostMapping("/{challengeId}/participate")
    public ResponseEntity<Map<String, Object>> participateInChallenge(@PathVariable Long challengeId) {
        UserEntity currentUser = userService.getCurrentUser();
        
        try {
            ChallengeEntity updatedChallenge = challengeService.participateInChallenge(challengeId, currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully joined the challenge");
            response.put("challenge", ChallengeResponse.from(updatedChallenge));
            response.put("participantCount", updatedChallenge.getParticipantCount());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Challenge not found");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{challengeId}/participate")
    public ResponseEntity<Map<String, Object>> leaveChallenge(@PathVariable Long challengeId) {
        UserEntity currentUser = userService.getCurrentUser();
        
        try {
            ChallengeEntity updatedChallenge = challengeService.leaveChallenge(challengeId, currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully left the challenge");
            response.put("challenge", ChallengeResponse.from(updatedChallenge));
            response.put("participantCount", updatedChallenge.getParticipantCount());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Challenge not found");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{challengeId}/participation-status")
    public ResponseEntity<Map<String, Object>> getParticipationStatus(@PathVariable Long challengeId) {
        UserEntity currentUser = userService.getCurrentUser();
        
        try {
            boolean isParticipant = challengeService.isParticipant(challengeId, currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isParticipant", isParticipant);
            response.put("userId", currentUser.getId());
            response.put("challengeId", challengeId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Challenge not found");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.notFound().build();
        }
    }
}