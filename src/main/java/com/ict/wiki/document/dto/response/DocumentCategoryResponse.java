package com.ict.wiki.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentCategoryResponse {
    private String code;         // enum name (WORK, INQUIRY 등)
    private String displayName;  // 화면 표시용 (업무, 민원 등)
    private String description;  // 설명
}