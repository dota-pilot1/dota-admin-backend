package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    // 비즈니스 로직 메서드
    public boolean isAdmin() {
        return "ADMIN".equals(this.name);
    }

    public boolean isUser() {
        return "USER".equals(this.name);
    }

    public boolean isDeveloper() {
        return "DEVELOPER".equals(this.name);
    }

    public void updateDetails(String newName, String newDescription) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName.trim().toUpperCase();
        }
        if (newDescription != null) {
            this.description = newDescription.trim();
        }
    }
}