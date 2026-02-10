package com.ict.wiki.document.controller;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.document.domain.DocumentCategory;
import com.ict.wiki.document.dto.request.DocumentCreateRequest;
import com.ict.wiki.document.dto.request.DocumentUpdateRequest;
import com.ict.wiki.document.dto.response.DocumentCategoryResponse;
import com.ict.wiki.document.dto.response.DocumentResponse;
import com.ict.wiki.document.dto.response.DocumentSummaryResponse;
import com.ict.wiki.document.service.DocumentService;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 위키 문서 API
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;  // ⭐ 추가

    /**
     * 현재 로그인한 사용자 조회 (헬퍼 메서드)
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 카테고리 목록 조회
     * GET /api/documents/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<DocumentCategoryResponse>> getAllCategories() {
        List<DocumentCategoryResponse> categories = Arrays.stream(DocumentCategory.values())
                .map(category -> new DocumentCategoryResponse(
                        category.name(),              // code: "WORK"
                        category.getDisplayName(),    // displayName: "업무"
                        category.getDescription()     // description: "업무 관련 문서"
                ))
                .toList();

        return ResponseEntity.ok(categories);
    }

    /**
     * 문서 생성
     * POST /api/documents
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User user = getCurrentUser(userDetails);

        Document document = documentService.createDocument(
                request.getTitle(),
                request.getContent(),
                request.getCategoryCode(),
                user
        );

        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * 문서 단건 조회 (조회수 증가)
     * GET /api/documents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentAndIncrementView(id);
        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * 제목으로 문서 조회 (조회수 증가 안 함)
     * GET /api/documents/by-title?title=Java 설치
     */
    @GetMapping("/by-title")
    public ResponseEntity<DocumentResponse> getDocumentByTitle(@RequestParam String title) {
        Document document = documentService.getDocumentByTitle(title);
        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * 모든 문서 목록 조회
     * GET /api/documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentSummaryResponse>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리별 문서 조회
     * GET /api/documents/category/{categoryCode}
     */
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<List<DocumentSummaryResponse>> getDocumentsByCategory(
            @PathVariable String categoryCode) {

        DocumentCategory category = DocumentCategory.fromCode(categoryCode);
        List<Document> documents = documentService.getDocumentsByCategory(category);
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 문서 검색 (제목 + 내용)
     * GET /api/documents/search?keyword=Java
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentSummaryResponse>> searchDocuments(
            @RequestParam String keyword) {

        List<Document> documents = documentService.searchByTitleOrContent(keyword);
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 인기 문서 조회 (조회수 상위)
     * GET /api/documents/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<List<DocumentSummaryResponse>> getPopularDocuments(
            @RequestParam(defaultValue = "10") int limit) {

        List<Document> documents = documentService.getPopularDocuments(limit);
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 생성 문서
     * GET /api/documents/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DocumentSummaryResponse>> getRecentDocuments(
            @RequestParam(defaultValue = "10") int limit) {

        List<Document> documents = documentService.getRecentDocuments(limit);
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 수정 문서
     * GET /api/documents/recently-updated?limit=10
     */
    @GetMapping("/recently-updated")
    public ResponseEntity<List<DocumentSummaryResponse>> getRecentlyUpdatedDocuments(
            @RequestParam(defaultValue = "10") int limit) {

        List<Document> documents = documentService.getRecentlyUpdatedDocuments(limit);
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 작성한 문서
     * GET /api/documents/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<DocumentSummaryResponse>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User user = getCurrentUser(userDetails);
        List<Document> documents = documentService.getDocumentsByAuthor(user.getId());
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 문서 수정
     * PUT /api/documents/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User user = getCurrentUser(userDetails);

        Document document = documentService.updateDocument(
                id,
                request.getTitle(),
                request.getContent(),
                request.getCategoryCode(),
                user,
                request.getEditReason()
        );

        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * 문서 삭제 (소프트 삭제)
     * DELETE /api/documents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User user = getCurrentUser(userDetails);
        documentService.deleteDocument(id, user);

        return ResponseEntity.ok(Map.of("message", "문서가 삭제되었습니다"));
    }

    /**
     * 문서 복원
     * POST /api/documents/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, String>> restoreDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {  // ⭐ 변경

        User user = getCurrentUser(userDetails);
        documentService.restoreDocument(id, user);

        return ResponseEntity.ok(Map.of("message", "문서가 복원되었습니다"));
    }

    /**
     * 삭제된 문서 목록 (관리자용)
     * GET /api/documents/deleted
     */
    @GetMapping("/deleted")
    public ResponseEntity<List<DocumentSummaryResponse>> getDeletedDocuments() {
        List<Document> documents = documentService.getDeletedDocuments();
        List<DocumentSummaryResponse> response = documents.stream()
                .map(DocumentSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}