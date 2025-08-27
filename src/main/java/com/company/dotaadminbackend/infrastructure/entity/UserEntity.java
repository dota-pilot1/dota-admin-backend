package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean kakaoNotificationConsent = false;
    
    // Business Logic Methods
    public boolean hasRole(String roleName) {
        return role != null && roleName.equals(role.getName());
    }
    
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    public boolean canReceiveNotifications() {
        return phoneNumber != null && kakaoNotificationConsent;
    }
    
    public void updateProfile(String newUsername, String newEmail) {
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            this.username = newUsername.trim();
        }
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            this.email = newEmail.trim();
        }
    }
    
    public void enableKakaoNotifications(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.kakaoNotificationConsent = true;
    }
    
    public void disableKakaoNotifications() {
        this.kakaoNotificationConsent = false;
    }
}