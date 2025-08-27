package com.company.dotaadminbackend.domain.challenge;

import com.company.dotaadminbackend.domain.reward.RewardType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "challenges")
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "challenge_tags", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "reward_amount")
    private Integer rewardAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type")
    private RewardType rewardType;

    @ElementCollection
    @CollectionTable(name = "challenge_participants", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "participant_id")
    private List<Long> participantIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Challenge() {}

    public Challenge(String title, String description, Long authorId,
                    LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ChallengeStatus.RECRUITING; // 기본값: 모집중
    }

    public void updateReward(Integer rewardAmount, RewardType rewardType) {
        this.rewardAmount = rewardAmount;
        this.rewardType = rewardType;
    }

    public void startChallenge() {
        this.status = ChallengeStatus.ACTIVE;
    }

    public void completeChallenge() {
        this.status = ChallengeStatus.COMPLETED;
    }

    public boolean isActive() {
        return ChallengeStatus.ACTIVE.equals(this.status);
    }

    public boolean isRecruiting() {
        return ChallengeStatus.RECRUITING.equals(this.status);
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getTags() { return tags; }
    public Long getAuthorId() { return authorId; }
    public Integer getRewardAmount() { return rewardAmount; }
    public RewardType getRewardType() { return rewardType; }
    public List<Long> getParticipantIds() { return participantIds; }
    public ChallengeStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters (필요한 것들만)
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
}
