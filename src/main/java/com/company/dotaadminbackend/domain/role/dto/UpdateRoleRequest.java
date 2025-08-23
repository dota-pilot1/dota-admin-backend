package com.company.dotaadminbackend.domain.role.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must be less than 50 characters")
    private String name;
    
    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;
}