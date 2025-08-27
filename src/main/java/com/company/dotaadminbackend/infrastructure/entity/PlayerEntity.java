package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    // Business Logic Methods
    public boolean hasValidName() {
        return name != null && !name.trim().isEmpty();
    }
    
    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        this.name = newName.trim();
    }
    
    public String getDisplayName() {
        return hasValidName() ? name : "Anonymous Player";
    }
}