package com.company.dotaadminbackend.infrastructure.adapter;

import com.company.dotaadminbackend.infrastructure.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    // slug로 문서 조회 (공개된 문서만)
    @Query("SELECT d FROM DocumentEntity d WHERE d.slug = :slug AND d.status = 'PUBLISHED'")
    Optional<DocumentEntity> findBySlugAndPublished(@Param("slug") String slug);

    // slug로 문서 조회 (모든 상태)
    Optional<DocumentEntity> findBySlug(String slug);

    // 카테고리별 공개 문서 조회
    @Query("SELECT d FROM DocumentEntity d WHERE d.category = :category AND d.status = 'PUBLISHED' ORDER BY d.displayOrder ASC, d.createdAt DESC")
    List<DocumentEntity> findByCategoryAndPublished(@Param("category") String category);

    // 추천 문서 조회
    @Query("SELECT d FROM DocumentEntity d WHERE d.isFeatured = true AND d.status = 'PUBLISHED' ORDER BY d.displayOrder ASC, d.viewCount DESC")
    List<DocumentEntity> findFeaturedDocuments();

    // 상태별 문서 조회 (페이징)
    Page<DocumentEntity> findByStatusOrderByDisplayOrderAscCreatedAtDesc(String status, Pageable pageable);

    // 전체 공개 문서 조회 (페이징)
    @Query("SELECT d FROM DocumentEntity d WHERE d.status = 'PUBLISHED' ORDER BY d.displayOrder ASC, d.createdAt DESC")
    Page<DocumentEntity> findPublishedDocuments(Pageable pageable);

    // 제목 또는 내용으로 검색 (공개 문서만)
    @Query("SELECT d FROM DocumentEntity d WHERE d.status = 'PUBLISHED' AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<DocumentEntity> searchPublishedDocuments(@Param("keyword") String keyword, Pageable pageable);

    // 조회수 상위 문서 조회
    @Query("SELECT d FROM DocumentEntity d WHERE d.status = 'PUBLISHED' ORDER BY d.viewCount DESC")
    List<DocumentEntity> findTopByViewCount(Pageable pageable);

    // 카테고리 목록 조회
    @Query("SELECT DISTINCT d.category FROM DocumentEntity d WHERE d.status = 'PUBLISHED' AND d.category IS NOT NULL ORDER BY d.category")
    List<String> findDistinctCategories();

    // slug 중복 확인
    boolean existsBySlug(String slug);
}