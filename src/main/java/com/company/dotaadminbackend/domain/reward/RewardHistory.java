package com.company.dotaadminbackend.domain.reward;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_histories")
public class RewardHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;
    
    @Column(name = "participant_id", nullable = false)
    private Long participantId;
    
    @Column(name = "reward_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal rewardAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 20)
    private RewardType rewardType;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    protected RewardHistory() {}
    
    public RewardHistory(Long challengeId, Long participantId, BigDecimal rewardAmount, RewardType rewardType) {
        this.challengeId = challengeId;
        this.participantId = participantId;
        this.rewardAmount = rewardAmount;
        this.rewardType = rewardType;
    }

    // Getters
    public Long getId() { return id; }
    public Long getChallengeId() { return challengeId; }
    public Long getParticipantId() { return participantId; }
    public BigDecimal getRewardAmount() { return rewardAmount; }
    public RewardType getRewardType() { return rewardType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}