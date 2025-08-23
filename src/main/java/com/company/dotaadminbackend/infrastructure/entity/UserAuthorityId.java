package com.company.dotaadminbackend.infrastructure.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorityId implements Serializable {
    private Long userId;
    private Long authorityId;
}