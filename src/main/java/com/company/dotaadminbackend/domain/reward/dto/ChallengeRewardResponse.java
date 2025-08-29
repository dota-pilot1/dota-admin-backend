package com.company.dotaadminbackend.domain.reward.dto;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeRewardEntity;
import com.company.dotaadminbackend.infrastructure.entity.ChallengeRewardMethod;

import java.time.LocalDateTime;

public class ChallengeRewardResponse {
    
    private Long id;
    private Long challengeId;
    private Long participantId;
    private String participantName; // 참가자 이름 (조인해서 가져옴)
    private Integer amount;
    private ChallengeRewardMethod method;
    private String reason;
    private LocalDateTime createdAt;
    private Long createdBy;
    private String createdByName; // 지급자 이름 (조인해서 가져옴)
    private Boolean processed;
    private LocalDateTime processedAt;
    
    // 기본 생성자
    public ChallengeRewardResponse() {}
    
    // Entity에서 Response로 변환하는 정적 메서드
    public static ChallengeRewardResponse from(ChallengeRewardEntity entity) {
        ChallengeRewardResponse response = new ChallengeRewardResponse();
        response.id = entity.getId();
        response.challengeId = entity.getChallengeId();
        response.participantId = entity.getParticipantId();
        response.amount = entity.getAmount();
        response.method = entity.getMethod();
        response.reason = entity.getReason();
        response.createdAt = entity.getCreatedAt();
        response.createdBy = entity.getCreatedBy();
        response.processed = entity.getProcessed();
        response.processedAt = entity.getProcessedAt();
        return response;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getChallengeId() { return challengeId; }
    public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
    
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }
    
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    
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
    
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    
    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
