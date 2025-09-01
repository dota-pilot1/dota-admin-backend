package com.company.dotaadminbackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String slug; // URL용 고유 키 (예: "auth-system", "api-reference")

    @Column(columnDefinition = "TEXT")
    private String content; // 마크다운 또는 HTML 콘텐츠

    @Column(length = 500)
    private String description; // 짧은 설명

    @Column(length = 50)
    private String category; // 카테고리 (예: "인증", "API", "가이드")

    @Column(length = 20)
    private String status; // 상태: DRAFT, PUBLISHED, ARCHIVED

    @Column(name = "view_count")
    private Long viewCount = 0L; // 조회수

    @Column(name = "is_featured")
    private boolean isFeatured = false; // 추천 문서 여부

    @Column(name = "display_order")
    private Integer displayOrder = 0; // 정렬 순서

    @Column(length = 100)
    private String icon; // 아이콘 (예: "🔐", "ShieldCheck")

    @Column(length = 200)
    private String tags; // 태그들 (JSON 또는 쉼표로 구분)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy; // 수정자

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 비즈니스 로직 메서드
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(this.status);
    }

    public boolean isDraft() {
        return "DRAFT".equals(this.status);
    }

    public boolean isArchived() {
        return "ARCHIVED".equals(this.status);
    }

    public String[] getTagsArray() {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    public void setTagsFromArray(String[] tagArray) {
        if (tagArray == null || tagArray.length == 0) {
            this.tags = "";
        } else {
            this.tags = String.join(",", tagArray);
        }
    }

    public void publishDocument() {
        this.status = "PUBLISHED";
    }

    public void archiveDocument() {
        this.status = "ARCHIVED";
    }

    public void makeDraft() {
        this.status = "DRAFT";
    }
}