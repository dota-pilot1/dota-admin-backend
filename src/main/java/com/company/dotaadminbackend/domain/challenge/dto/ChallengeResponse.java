package com.company.dotaadminbackend.domain.challenge.dto;

import com.company.dotaadminbackend.infrastructure.entity.ChallengeEntity;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import com.company.dotaadminbackend.domain.reward.RewardType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeResponse {
    
    private Long id;
    private String title;
    private String description;
    private Long authorId;
    private String author; // 작성자 이름 추가
    private List<String> tags;
    private List<Long> participantIds;
    private List<ParticipantResponse> participants;
    private Integer participantCount;
    private ChallengeStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rewardAmount;
    private RewardType rewardType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ChallengeResponse from(ChallengeEntity challenge) {
        return new ChallengeResponse(
            challenge.getId(),
            challenge.getTitle(),
            challenge.getDescription(),
            challenge.getAuthorId(),
            null, // author will be set by service layer
            challenge.getTags(),
            challenge.getParticipantIds(),
            null, // participants will be set by service layer
            challenge.getParticipantCount(),
            challenge.getStatus(),
            challenge.getStartDate(),
            challenge.getEndDate(),
            challenge.getRewardAmount(),
            challenge.getRewardType(),
            challenge.getCreatedAt(),
            challenge.getUpdatedAt()
        );
    }
}