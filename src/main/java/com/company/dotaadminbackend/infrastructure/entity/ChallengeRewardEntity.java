package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_rewards")
public class ChallengeRewardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;
    
    @Column(name = "participant_id", nullable = false)
    private Long participantId;
    
    @Column(nullable = false)
    private Integer amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeRewardMethod method;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 포상을 지급한 관리자 ID
    
    @Column(name = "processed", nullable = false)
    private Boolean processed = false; // 포상 처리 완료 여부
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // 기본 생성자
    public ChallengeRewardEntity() {}
    
    // 생성자
    public ChallengeRewardEntity(Long challengeId, Long participantId, Integer amount, 
                       ChallengeRewardMethod method, String reason, Long createdBy) {
        this.challengeId = challengeId;
        this.participantId = participantId;
        this.amount = amount;
        this.method = method;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.processed = false;
    }
    
    // 포상 처리 완료 메서드
    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getChallengeId() { return challengeId; }
    public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
    
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public ChallengeRewardMethod getMethod() { return method; }
    public void setMethod(ChallengeRewardMethod method) { this.method = method; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
