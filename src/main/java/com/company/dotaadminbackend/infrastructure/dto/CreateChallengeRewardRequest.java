package com.company.dotaadminbackend.infrastructure.dto;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeRewardMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateChallengeRewardRequest {
    
    @NotNull(message = "참가자 ID는 필수입니다")
    private Long participantId;
    
    @NotNull(message = "포상 금액은 필수입니다")
    @Min(value = 1, message = "포상 금액은 1 이상이어야 합니다")
    private Integer amount;
    
    @NotNull(message = "포상 방법은 필수입니다")
    private ChallengeRewardMethod method;
    
    @NotBlank(message = "포상 사유는 필수입니다")
    private String reason;
    
    // 기본 생성자
    public CreateChallengeRewardRequest() {}
    
    // 생성자
    public CreateChallengeRewardRequest(Long participantId, Integer amount, ChallengeRewardMethod method, String reason) {
        this.participantId = participantId;
        this.amount = amount;
        this.method = method;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public ChallengeRewardMethod getMethod() { return method; }
    public void setMethod(ChallengeRewardMethod method) { this.method = method; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
