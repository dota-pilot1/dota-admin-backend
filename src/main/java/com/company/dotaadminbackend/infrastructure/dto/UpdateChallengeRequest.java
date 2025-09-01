package com.company.dotaadminbackend.infrastructure.dto;

import com.company.dotaadminbackend.domain.reward.RewardType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChallengeRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private List<String> tags;
    
    @Min(value = 0, message = "Reward amount must be non-negative")
    private Integer rewardAmount;
    
    private RewardType rewardType;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
