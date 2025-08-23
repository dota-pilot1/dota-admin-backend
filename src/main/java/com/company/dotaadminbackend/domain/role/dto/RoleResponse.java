package com.company.dotaadminbackend.domain.role.dto;

import com.company.dotaadminbackend.domain.model.Role;
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
    
    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getId(),
            role.getName(),
            role.getDescription()
        );
    }
}