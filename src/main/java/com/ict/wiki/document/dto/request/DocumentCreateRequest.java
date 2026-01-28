package com.ict.wiki.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 문서 생성 요청
 */
@Getter
@Setter
public class DocumentCreateRequest {

    @NotBlank(message = "제목을 입력하세요")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용을 입력하세요")
    private String content;
}