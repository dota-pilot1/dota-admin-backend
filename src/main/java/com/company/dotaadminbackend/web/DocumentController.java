package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.DocumentService;
import com.company.dotaadminbackend.infrastructure.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // 공개 문서 단건 조회 (조회수 증가)
    @GetMapping("/public/{slug}")
    public ResponseEntity<DocumentEntity> getPublishedDocument(@PathVariable String slug) {
        Optional<DocumentEntity> document = documentService.getPublishedDocument(slug);
        return document.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 공개 문서 목록 조회
    @GetMapping("/public")
    public ResponseEntity<Page<DocumentEntity>> getPublishedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DocumentEntity> documents = documentService.getPublishedDocuments(page, size);
        return ResponseEntity.ok(documents);
    }

    // 카테고리별 공개 문서 조회
    @GetMapping("/public/category/{category}")
    public ResponseEntity<List<DocumentEntity>> getDocumentsByCategory(@PathVariable String category) {
        List<DocumentEntity> documents = documentService.getDocumentsByCategory(category);
        return ResponseEntity.ok(documents);
    }

    // 추천 문서 조회
    @GetMapping("/public/featured")
    public ResponseEntity<List<DocumentEntity>> getFeaturedDocuments() {
        List<DocumentEntity> documents = documentService.getFeaturedDocuments();
        return ResponseEntity.ok(documents);
    }

    // 인기 문서 조회
    @GetMapping("/public/popular")
    public ResponseEntity<List<DocumentEntity>> getPopularDocuments(
            @RequestParam(defaultValue = "10") int limit) {
        List<DocumentEntity> documents = documentService.getPopularDocuments(limit);
        return ResponseEntity.ok(documents);
    }

    // 문서 검색
    @GetMapping("/public/search")
    public ResponseEntity<Page<DocumentEntity>> searchDocuments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DocumentEntity> documents = documentService.searchDocuments(keyword, page, size);
        return ResponseEntity.ok(documents);
    }

    // 카테고리 목록 조회
    @GetMapping("/public/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = documentService.getCategories();
        return ResponseEntity.ok(categories);
    }

    // === 관리자 전용 API ===

    // 관리자용 문서 단건 조회 (모든 상태)
    @GetMapping("/admin/{slug}")
    // @PreAuthorize("hasRole('ADMIN')") // 추후 권한 설정 시 주석 해제
    public ResponseEntity<DocumentEntity> getDocument(@PathVariable String slug) {
        Optional<DocumentEntity> document = documentService.getDocument(slug);
        return document.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 관리자용 모든 문서 조회
    @GetMapping("/admin")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DocumentEntity>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DocumentEntity> documents = documentService.getAllDocuments(page, size);
        return ResponseEntity.ok(documents);
    }

    // 상태별 문서 조회
    @GetMapping("/admin/status/{status}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DocumentEntity>> getDocumentsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DocumentEntity> documents = documentService.getDocumentsByStatus(status, page, size);
        return ResponseEntity.ok(documents);
    }

    // 문서 생성
    @PostMapping("/admin")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentEntity> createDocument(@RequestBody DocumentEntity document) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            DocumentEntity createdDocument = documentService.createDocument(document, userEmail);
            return ResponseEntity.ok(createdDocument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 문서 수정
    @PutMapping("/admin/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentEntity> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentEntity document) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            DocumentEntity updatedDocument = documentService.updateDocument(id, document, userEmail);
            return ResponseEntity.ok(updatedDocument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 문서 삭제
    @DeleteMapping("/admin/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}