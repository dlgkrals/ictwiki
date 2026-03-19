package com.ict.wiki.document.controller;

import com.ict.wiki.document.domain.DocumentLink;
import com.ict.wiki.document.dto.response.DocumentLinkResponse;
import com.ict.wiki.document.service.DocumentLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 링크 (역참조) API
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentLinkController {

    private final DocumentLinkService linkService;

    /**
     * 역참조 조회 - 이 문서를 링크하는 문서들 (들어오는 링크)
     * GET /api/documents/{id}/backlinks
     */
    @GetMapping("/{id}/backlinks")
    public ResponseEntity<List<DocumentLinkResponse>> getBacklinks(@PathVariable Long id) {
        List<DocumentLink> links = linkService.getIncomingLinks(id);
        List<DocumentLinkResponse> response = links.stream()
                .map(DocumentLinkResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 나가는 링크 조회 - 이 문서가 링크하는 문서들
     * GET /api/documents/{id}/outlinks
     */
    @GetMapping("/{id}/outlinks")
    public ResponseEntity<List<DocumentLinkResponse>> getOutlinks(@PathVariable Long id) {
        List<DocumentLink> links = linkService.getOutgoingLinks(id);
        List<DocumentLinkResponse> response = links.stream()
                .map(DocumentLinkResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 역참조 수 조회
     * GET /api/documents/{id}/backlinks/count
     */
    @GetMapping("/{id}/backlinks/count")
    public ResponseEntity<Long> countBacklinks(@PathVariable Long id) {
        long count = linkService.countIncomingLinks(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 나가는 링크 수 조회
     * GET /api/documents/{id}/outlinks/count
     */
    @GetMapping("/{id}/outlinks/count")
    public ResponseEntity<Long> countOutlinks(@PathVariable Long id) {
        long count = linkService.countOutgoingLinks(id);
        return ResponseEntity.ok(count);
    }
}