package com.company.dotaadminbackend.domain.reward;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_rewards")
public class RewardHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;
    
    @Column(name = "participant_id", nullable = false)
    private Long participantId;
    
    @Column(name = "amount", nullable = false)
    private Integer amount; // DB는 integer 타입
    
    @Column(name = "processed", nullable = false)
    private Boolean processed = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private RewardType rewardType;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    protected RewardHistory() {}
    
    public RewardHistory(Long challengeId, Long participantId, Integer amount, RewardType rewardType) {
        this.challengeId = challengeId;
        this.participantId = participantId;
        this.amount = amount;
        this.rewardType = rewardType;
        this.processed = false;
        this.createdBy = participantId; // 임시로 participantId를 createdBy로 사용
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getChallengeId() { return challengeId; }
    public Long getParticipantId() { return participantId; }
    public Integer getAmount() { return amount; }
    public Boolean getProcessed() { return processed; }
    public RewardType getRewardType() { return rewardType; }
    public String getReason() { return reason; }
    public Long getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    
    public void setProcessed(Boolean processed) { this.processed = processed; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public void setReason(String reason) { this.reason = reason; }
}