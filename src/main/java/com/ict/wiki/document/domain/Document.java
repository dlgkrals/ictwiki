package com.ict.wiki.document.domain;

import com.ict.wiki.BaseEntity;
import com.ict.wiki.login.domain.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 위키 문서 엔티티
 * - 마크다운 형식 지원
 * - 문서 간 링크 지원 [[문서제목]]
 * - 버전 관리 (수정 이력)
 */
@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문서 제목 (활성 문서 내에서만 고유)
     * - URL 경로로도 사용
     * - 예: "Java 설치 가이드"
     * - 소프트 삭제된 문서와는 제목이 겹칠 수 있음 (DB에는 deleted=false 조건의 partial unique index로 적용)
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 문서 내용 (마크다운)
     * - 마크다운 형식으로 저장
     * - 위키 링크: [[문서제목]] 또는 [[표시텍스트|문서제목]]
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 문서 카테고리
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentCategory category;

    /**
     * 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * 조회수
     */
    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 문서 버전 (수정 횟수)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * 마지막 수정자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_editor_id")
    private User lastEditor;

    /**
     * 삭제 여부 (소프트 삭제)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    // ========== 비즈니스 메서드 ==========

    /**
     * 문서 수정
     */
    public void update(String title, String content, DocumentCategory category, User editor) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.lastEditor = editor;
        this.version++;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 문서 삭제 (소프트 삭제)
     */
    public void delete() {
        this.deleted = true;
    }

    /**
     * 문서 복원
     */
    public void restore() {
        this.deleted = false;
    }
}