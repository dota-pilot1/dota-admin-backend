package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "authorities")
public class AuthorityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    // 비즈니스 로직 메서드
    public boolean isReadAuthority() {
        return this.name != null && this.name.endsWith("_READ");
    }

    public boolean isWriteAuthority() {
        return this.name != null && this.name.endsWith("_WRITE");
    }

    public boolean isDeleteAuthority() {
        return this.name != null && this.name.endsWith("_DELETE");
    }

    public boolean isManageAuthority() {
        return this.name != null && this.name.endsWith("_MANAGE");
    }

    public boolean belongsToCategory(String categoryName) {
        return this.category != null && this.category.equalsIgnoreCase(categoryName);
    }

    public String getPermissionLevel() {
        if (isManageAuthority()) return "MANAGE";
        if (isDeleteAuthority()) return "DELETE";
        if (isWriteAuthority()) return "WRITE";
        if (isReadAuthority()) return "READ";
        return "UNKNOWN";
    }
}