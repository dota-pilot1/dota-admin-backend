package com.company.dotaadminbackend.infrastructure.dto;

import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    
    private Long id;
    private String name;
    private String description;
    
    public static RoleResponse from(RoleEntity roleEntity) {
        return new RoleResponse(
            roleEntity.getId(),
            roleEntity.getName(),
            roleEntity.getDescription()
        );
    }
}