package com.company.dotaadminbackend.infrastructure.entity;

public enum ChallengeRewardMethod {
    POINT("포인트"),
    CASH("현금");
    
    private final String description;
    
    ChallengeRewardMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
