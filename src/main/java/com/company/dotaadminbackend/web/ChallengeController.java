package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.ChallengeService;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.domain.challenge.Challenge;
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
        UserEntity currentUserEntity = userService.getCurrentUserEntity();
        Challenge challenge = challengeService.createChallenge(request, currentUserEntity.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Challenge created successfully");
        response.put("challenge", ChallengeResponse.from(challenge));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<Map<String, Object>> getChallenge(@PathVariable Long challengeId) {
        Challenge challenge = challengeService.getChallengeById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found with id: " + challengeId));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("challenge", ChallengeResponse.from(challenge));
        return ResponseEntity.ok(response);
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
}