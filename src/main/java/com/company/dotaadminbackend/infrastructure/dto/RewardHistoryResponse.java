package com.company.dotaadminbackend.infrastructure.dto;

import com.company.dotaadminbackend.domain.reward.RewardHistory;
import com.company.dotaadminbackend.domain.reward.RewardType;

import java.time.LocalDateTime;

public class RewardHistoryResponse {
    private Long id;
    private Long challengeId;
    private Long participantId;
    private String participantEmail;
    private String participantUsername;
    private Integer rewardAmount;
    private RewardType rewardType;
    private LocalDateTime createdAt;

    public RewardHistoryResponse() {}

    public RewardHistoryResponse(Long id, Long challengeId, Long participantId,
                               String participantEmail, String participantUsername,
                               Integer rewardAmount, RewardType rewardType,
                               LocalDateTime createdAt) {
        this.id = id;
        this.challengeId = challengeId;
        this.participantId = participantId;
        this.participantEmail = participantEmail;
        this.participantUsername = participantUsername;
        this.rewardAmount = rewardAmount;
        this.rewardType = rewardType;
        this.createdAt = createdAt;
    }

    public static RewardHistoryResponse from(RewardHistory rewardHistory) {
        return new RewardHistoryResponse(
            rewardHistory.getId(),
            rewardHistory.getChallengeId(),
            rewardHistory.getParticipantId(),
            null, // will be filled by service layer
            null,
            rewardHistory.getAmount(),
            rewardHistory.getRewardType(),
            rewardHistory.getCreatedAt()
        );
    }

    public static RewardHistoryResponse fromWithUserInfo(
        RewardHistory rewardHistory,
        String participantEmail,
        String participantUsername
    ) {
        return new RewardHistoryResponse(
            rewardHistory.getId(),
            rewardHistory.getChallengeId(),
            rewardHistory.getParticipantId(),
            participantEmail,
            participantUsername,
            rewardHistory.getAmount(),
            rewardHistory.getRewardType(),
            rewardHistory.getCreatedAt()
        );
    }

    // Getters
    public Long getId() { return id; }
    public Long getChallengeId() { return challengeId; }
    public Long getParticipantId() { return participantId; }
    public String getParticipantEmail() { return participantEmail; }
    public String getParticipantUsername() { return participantUsername; }
    public Integer getRewardAmount() { return rewardAmount; }
    public RewardType getRewardType() { return rewardType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
