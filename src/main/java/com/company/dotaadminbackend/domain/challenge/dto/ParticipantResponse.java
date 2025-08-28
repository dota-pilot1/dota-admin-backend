package com.company.dotaadminbackend.domain.challenge.dto;

import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private Long id;
    private String name;
    private String email;
    private String achievedAt; // 달성 날짜가 있다면
    
    public static ParticipantResponse from(UserEntity user) {
        return new ParticipantResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            null // 달성 날짜는 별도 로직으로 처리
        );
    }
}