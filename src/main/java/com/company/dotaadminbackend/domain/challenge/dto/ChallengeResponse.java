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
    private String username; // 작성자 username
    private String email; // 작성자 이메일
    private List<String> tags;
    private List<Long> participantIds;
    private List<ParticipantResponse> participants;
    private Integer participantCount;
    private ChallengeStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rewardAmount;
    private RewardType rewardType;
    private Long rewardedParticipantCount; // 포상 받은 참가자 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ChallengeResponse from(ChallengeEntity challenge) {
        return new ChallengeResponse(
            challenge.getId(),
            challenge.getTitle(),
            challenge.getDescription(),
            challenge.getAuthorId(),
            null, // username will be set by service layer
            null, // email will be set by service layer
            challenge.getTags(),
            challenge.getParticipantIds(),
            null, // participants will be set by service layer
            challenge.getParticipantCount(),
            challenge.getStatus(),
            challenge.getStartDate(),
            challenge.getEndDate(),
            challenge.getRewardAmount(),
            challenge.getRewardType(),
            0L, // rewardedParticipantCount will be set by service layer
            challenge.getCreatedAt(),
            challenge.getUpdatedAt()
        );
    }
}