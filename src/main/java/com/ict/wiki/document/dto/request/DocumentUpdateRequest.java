package com.ict.wiki.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 문서 수정 요청
 */
@Getter
@Setter
public class DocumentUpdateRequest {

    @NotBlank(message = "제목을 입력하세요")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용을 입력하세요")
    private String content;

    @NotBlank(message = "카테고리를 선택하세요")
    private String categoryCode;

    @Size(max = 500, message = "수정 사유는 500자 이하여야 합니다")
    private String editReason;
}