package com.company.dotaadminbackend.infrastructure.entity;

public enum ChallengeRewardMethod {
    CASH("현금"),
    ITEM("아이템");

    private final String description;

    ChallengeRewardMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
