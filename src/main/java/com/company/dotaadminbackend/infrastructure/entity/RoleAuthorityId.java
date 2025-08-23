package com.company.dotaadminbackend.infrastructure.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAuthorityId implements Serializable {
    private Long roleId;
    private Long authorityId;
}