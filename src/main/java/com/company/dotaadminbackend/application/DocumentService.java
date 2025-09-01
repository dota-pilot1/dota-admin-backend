package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.infrastructure.adapter.DocumentRepository;
import com.company.dotaadminbackend.infrastructure.entity.DocumentEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserService userService;

    public DocumentService(DocumentRepository documentRepository, UserService userService) {
        this.documentRepository = documentRepository;
        this.userService = userService;
    }

    // 공개 문서 조회 (조회수 증가)
    @Transactional
    public Optional<DocumentEntity> getPublishedDocument(String slug) {
        Optional<DocumentEntity> document = documentRepository.findBySlugAndPublished(slug);
        document.ifPresent(doc -> {
            doc.incrementViewCount();
            documentRepository.save(doc);
        });
        return document;
    }

    // 관리자용 문서 조회 (모든 상태)
    public Optional<DocumentEntity> getDocument(String slug) {
        return documentRepository.findBySlug(slug);
    }

    // 모든 공개 문서 목록 조회
    public Page<DocumentEntity> getPublishedDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findPublishedDocuments(pageable);
    }

    // 카테고리별 문서 목록
    public List<DocumentEntity> getDocumentsByCategory(String category) {
        return documentRepository.findByCategoryAndPublished(category);
    }

    // 추천 문서 목록
    public List<DocumentEntity> getFeaturedDocuments() {
        return documentRepository.findFeaturedDocuments();
    }

    // 인기 문서 목록 (조회수 기준)
    public List<DocumentEntity> getPopularDocuments(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return documentRepository.findTopByViewCount(pageable);
    }

    // 문서 검색
    public Page<DocumentEntity> searchDocuments(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.searchPublishedDocuments(keyword, pageable);
    }

    // 카테고리 목록 조회
    public List<String> getCategories() {
        return documentRepository.findDistinctCategories();
    }

    // 문서 생성 (관리자)
    @Transactional
    public DocumentEntity createDocument(DocumentEntity document, String userEmail) {
        Optional<UserEntity> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            document.setCreatedBy(user.get());
            document.setUpdatedBy(user.get());
        }

        // slug 중복 확인
        if (documentRepository.existsBySlug(document.getSlug())) {
            throw new IllegalArgumentException("이미 존재하는 슬러그입니다: " + document.getSlug());
        }

        return documentRepository.save(document);
    }

    // 문서 수정 (관리자)
    @Transactional
    public DocumentEntity updateDocument(Long id, DocumentEntity updatedDocument, String userEmail) {
        DocumentEntity existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + id));

        // slug 변경 시 중복 확인
        if (!existingDocument.getSlug().equals(updatedDocument.getSlug())) {
            if (documentRepository.existsBySlug(updatedDocument.getSlug())) {
                throw new IllegalArgumentException("이미 존재하는 슬러그입니다: " + updatedDocument.getSlug());
            }
        }

        Optional<UserEntity> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            existingDocument.setUpdatedBy(user.get());
        }

        // 필드 업데이트
        existingDocument.setTitle(updatedDocument.getTitle());
        existingDocument.setSlug(updatedDocument.getSlug());
        existingDocument.setContent(updatedDocument.getContent());
        existingDocument.setDescription(updatedDocument.getDescription());
        existingDocument.setCategory(updatedDocument.getCategory());
        existingDocument.setStatus(updatedDocument.getStatus());
        existingDocument.setFeatured(updatedDocument.isFeatured());
        existingDocument.setDisplayOrder(updatedDocument.getDisplayOrder());
        existingDocument.setIcon(updatedDocument.getIcon());
        existingDocument.setTags(updatedDocument.getTags());

        return documentRepository.save(existingDocument);
    }

    // 문서 삭제 (관리자)
    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new IllegalArgumentException("문서를 찾을 수 없습니다: " + id);
        }
        documentRepository.deleteById(id);
    }

    // 관리자용 모든 문서 조회
    public Page<DocumentEntity> getAllDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findAll(pageable);
    }

    // 상태별 문서 조회
    public Page<DocumentEntity> getDocumentsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findByStatusOrderByDisplayOrderAscCreatedAtDesc(status, pageable);
    }
}