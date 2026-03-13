package com.ict.wiki.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RagErrorCode implements ErrorCode {

    EMBEDDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RAG_001", "임베딩 생성에 실패했습니다"),
    CHAT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RAG_002", "AI 답변 생성에 실패했습니다"),
    CHUNK_NOT_FOUND(HttpStatus.NOT_FOUND, "RAG_003", "RAG 청크를 찾을 수 없습니다"),
    SIMILAR_CASE_NOT_FOUND(HttpStatus.NOT_FOUND, "RAG_004", "유사 사례를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getFullCode() {
        return code;
    }
}