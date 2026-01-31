package com.ict.wiki.notice.domain;

import com.ict.wiki.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 엔티티
 * - 단순 CRUD
 * - 제목, 내용만 관리
 */
@Entity
@Table(name = "notices",
        indexes = {
                @Index(name = "idx_created_at", columnList = "created_at")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 내용 (마크다운)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // ========== 비즈니스 메서드 ==========

    /**
     * 공지사항 수정
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}