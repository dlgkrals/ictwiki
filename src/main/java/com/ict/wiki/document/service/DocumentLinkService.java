package com.ict.wiki.document.service;

import com.ict.wiki.document.domain.Document;
import com.ict.wiki.document.domain.DocumentLink;
import com.ict.wiki.document.events.DocumentCreatedEvent;
import com.ict.wiki.document.events.DocumentUpdatedEvent;
import com.ict.wiki.document.repository.DocumentLinkRepository;
import com.ict.wiki.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 위키 문서 링크 서비스 (이벤트 기반)
 * - 이벤트를 받아서 링크 처리
 * - 링크 조회 기능
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentLinkService {

    private final DocumentLinkRepository linkRepository;
    private final DocumentRepository documentRepository;

    // 위키 링크 정규식: [[표시텍스트|문서제목]] 또는 [[문서제목]]
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[([^\\]|]+)(?:\\|([^\\]]+))?\\]\\]");

    /**
     * 문서 생성 시 링크 처리 (이벤트 처리용)
     */
    @Transactional
    public void processDocumentLinks(DocumentCreatedEvent event) {
        Document sourceDocument = documentRepository.findById(event.getDocumentId())
                .orElseThrow(() -> new IllegalStateException("문서를 찾을 수 없습니다: " + event.getDocumentId()));
        parseAndSaveLinks(sourceDocument, event.getContent());
    }

    /**
     * 문서 수정 시 링크 업데이트 (이벤트 처리용)
     */
    @Transactional
    public void updateDocumentLinks(DocumentUpdatedEvent event) {
        linkRepository.deleteBySourceDocumentId(event.getDocumentId());
        log.debug("기존 링크 삭제 완료 - DocumentId: {}", event.getDocumentId());

        Document sourceDocument = documentRepository.findById(event.getDocumentId())
                .orElseThrow(() -> new IllegalStateException("문서를 찾을 수 없습니다: " + event.getDocumentId()));
        parseAndSaveLinks(sourceDocument, event.getNewContent());
    }

    /**
     * 문서 삭제 시 모든 링크 삭제 (이벤트 처리용)
     */
    @Transactional
    public void deleteAllDocumentLinks(Long documentId) {
        linkRepository.deleteAllByDocumentId(documentId);
        log.debug("모든 링크 삭제 완료 - DocumentId: {}", documentId);
    }

    /**
     * 링크 파싱 및 저장 (내부 메서드)
     */
    private void parseAndSaveLinks(Document sourceDocument, String content) {
        List<WikiLink> wikiLinks = parseWikiLinks(content);
        int linkCount = 0;

        for (WikiLink wikiLink : wikiLinks) {
            Optional<Document> targetDocumentOpt = documentRepository.findByTitleIgnoreCase(wikiLink.targetTitle);

            if (targetDocumentOpt.isPresent()) {
                Document targetDocument = targetDocumentOpt.get();

                // 중복 체크
                Optional<DocumentLink> existingLink = linkRepository.findBySourceAndTarget(
                        sourceDocument.getId(), targetDocument.getId());

                if (existingLink.isEmpty()) {
                    DocumentLink link = DocumentLink.of(sourceDocument, targetDocument, wikiLink.displayText);
                    linkRepository.save(link);
                    linkCount++;
                    log.debug("링크 생성 - From: {} → To: {}",
                            sourceDocument.getTitle(), targetDocument.getTitle());
                }
            } else {
                log.debug("링크 대상 문서 없음 - Target: {}", wikiLink.targetTitle);
            }
        }

        log.debug("링크 처리 완료 - DocumentId: {}, 생성된 링크: {}개",
                sourceDocument.getId(), linkCount);
    }

    /**
     * 문서 내용에서 위키 링크 파싱
     */
    public List<WikiLink> parseWikiLinks(String content) {
        List<WikiLink> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(content);

        while (matcher.find()) {
            String first = matcher.group(1);
            String second = matcher.group(2);

            String displayText;
            String targetTitle;

            if (second != null) {
                // [[표시텍스트|문서제목]] 형식
                displayText = first;
                targetTitle = second;
            } else {
                // [[문서제목]] 형식
                displayText = first;
                targetTitle = first;
            }

            links.add(new WikiLink(displayText, targetTitle));
        }

        return links;
    }

    /**
     * 특정 문서가 링크하는 문서들 조회 (나가는 링크)
     */
    public List<DocumentLink> getOutgoingLinks(Long documentId) {
        return linkRepository.findBySourceDocumentId(documentId);
    }

    /**
     * 특정 문서를 링크하는 문서들 조회 (들어오는 링크, 역참조)
     */
    public List<DocumentLink> getIncomingLinks(Long documentId) {
        return linkRepository.findByTargetDocumentId(documentId);
    }

    /**
     * 나가는 링크 개수
     */
    public long countOutgoingLinks(Long documentId) {
        return linkRepository.countBySourceDocumentId(documentId);
    }

    /**
     * 들어오는 링크 개수 (역참조 수)
     */
    public long countIncomingLinks(Long documentId) {
        return linkRepository.countByTargetDocumentId(documentId);
    }

    /**
     * 가장 많이 참조되는 문서 (인기 문서)
     */
    public List<Object[]> getMostLinkedDocuments(int limit) {
        return linkRepository.findMostLinkedDocuments(limit);
    }

    /**
     * 고아 문서 찾기 (들어오는 링크가 없는 문서)
     */
    public List<Document> findOrphanDocuments() {
        List<Document> allDocuments = documentRepository.findAllNotDeleted();
        List<Document> orphans = new ArrayList<>();

        for (Document doc : allDocuments) {
            long incomingLinks = countIncomingLinks(doc.getId());
            if (incomingLinks == 0) {
                orphans.add(doc);
            }
        }

        log.info("고아 문서 개수: {}", orphans.size());
        return orphans;
    }

    /**
     * 끊어진 링크 찾기 (존재하지 않는 문서로의 링크)
     */
    public List<BrokenLink> findBrokenLinks(String content, Long sourceDocumentId) {
        List<BrokenLink> brokenLinks = new ArrayList<>();
        List<WikiLink> wikiLinks = parseWikiLinks(content);

        for (WikiLink wikiLink : wikiLinks) {
            Optional<Document> target = documentRepository.findByTitleIgnoreCase(wikiLink.targetTitle);
            if (target.isEmpty()) {
                brokenLinks.add(new BrokenLink(sourceDocumentId, wikiLink.targetTitle));
            }
        }

        return brokenLinks;
    }

    // ========== 내부 클래스 ==========

    /**
     * 파싱된 위키 링크 정보
     */
    public static class WikiLink {
        public final String displayText;
        public final String targetTitle;

        public WikiLink(String displayText, String targetTitle) {
            this.displayText = displayText;
            this.targetTitle = targetTitle;
        }
    }

    /**
     * 끊어진 링크 정보
     */
    public static class BrokenLink {
        public final Long sourceDocumentId;
        public final String targetTitle;

        public BrokenLink(Long sourceDocumentId, String targetTitle) {
            this.sourceDocumentId = sourceDocumentId;
            this.targetTitle = targetTitle;
        }
    }
}