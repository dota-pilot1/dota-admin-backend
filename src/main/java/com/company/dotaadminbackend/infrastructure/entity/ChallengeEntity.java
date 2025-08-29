package com.company.dotaadminbackend.infrastructure.entity;

import com.company.dotaadminbackend.domain.reward.RewardType;
import com.company.dotaadminbackend.domain.challenge.ChallengeStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
public class ChallengeEntity {
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
    private List<Long> participantIds = new ArrayList<>();

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

    protected ChallengeEntity() {}

    public ChallengeEntity(String title, String description, Long authorId, 
                    LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ChallengeStatus.RECRUITING; // 기본값: 모집중
        this.participantIds = new ArrayList<>();
    }

    // Business Logic Methods
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

    // Participation Management Methods
    public boolean canParticipate() {
        return isRecruiting() && !isExpired();
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    public boolean isParticipant(Long userId) {
        return participantIds != null && participantIds.contains(userId);
    }

    public boolean isAuthor(Long userId) {
        return authorId.equals(userId);
    }

    public void addParticipant(Long userId) {
        if (isParticipant(userId)) {
            throw new IllegalStateException("이미 참여한 챌린지입니다.");
        }
        if (isAuthor(userId)) {
            throw new IllegalStateException("작성자는 챌린지에 참여할 수 없습니다.");
        }
        if (isExpired()) {
            throw new IllegalStateException("챌린지 기간이 종료되어 참여할 수 없습니다.");
        }
        if (!isRecruiting()) {
            throw new IllegalStateException("챌린지 모집 상태가 아닙니다.");
        }
        if (!canParticipate()) {
            throw new IllegalStateException("챌린지에 참여할 수 없는 상태입니다.");
        }
        if (participantIds == null) {
            participantIds = new ArrayList<>();
        }
        participantIds.add(userId);
    }


    public void removeParticipant(Long userId) {
        if (participantIds != null) {
            participantIds.remove(userId);
        }
    }

    public int getParticipantCount() {
        return participantIds != null ? participantIds.size() : 0;
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