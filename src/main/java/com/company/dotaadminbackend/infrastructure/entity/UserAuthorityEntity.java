package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_authorities")
@IdClass(UserAuthorityId.class)
public class UserAuthorityEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "authority_id")
    private Long authorityId;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt = LocalDateTime.now();

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", insertable = false, updatable = false)
    private AuthorityEntity authority;
}