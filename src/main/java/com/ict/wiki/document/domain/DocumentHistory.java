package com.ict.wiki.document.domain;

import com.ict.wiki.login.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 문서 수정 이력 (버전 관리)
 * - 모든 수정 내역 저장
 * - 이전 버전 복원 가능
 */
@Entity
@Table(name = "document_histories",
        indexes = {
                // 성능 최적화: "특정 문서의 수정 이력 조회" 쿼리 속도 향상
                @Index(name = "idx_document_id", columnList = "document_id"),
                // 성능 최적화: "특정 문서의 특정 버전 조회" 쿼리 속도 향상
                @Index(name = "idx_version", columnList = "document_id, version")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 원본 문서
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * 버전 번호
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * 당시 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 당시 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    @Lob
    private String content;

    /**
     * 수정자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    private User editor;

    /**
     * 수정 사유 (선택)
     */
    @Column(length = 500)
    private String editReason;

    /**
     * 수정 시간
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime editedAt;

    // ========== 팩토리 메서드 ==========

    /**
     * 문서 수정 이력 생성
     */
    public static DocumentHistory from(Document document, User editor, String editReason) {
        return DocumentHistory.builder()
                .document(document)
                .version(document.getVersion())
                .title(document.getTitle())
                .content(document.getContent())
                .editor(editor)
                .editReason(editReason)
                .build();
    }
}