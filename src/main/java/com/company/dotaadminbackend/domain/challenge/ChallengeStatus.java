package com.company.dotaadminbackend.domain.challenge;

public enum ChallengeStatus {
    RECRUITING("모집중"),    // 모집중
    IN_PROGRESS("진행중"),   // 진행중
    COMPLETED("완료");       // 완료
    
    private final String description;
    
    ChallengeStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}