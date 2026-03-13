package com.ict.wiki.rag.service;

import com.ict.wiki.exception.code.RagErrorCode;
import com.ict.wiki.exception.custom.RagException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * OpenAI text-embedding-3-small 임베딩 서비스
 * - 512차원 (dimensions 파라미터로 축소)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private static final String MODEL = "text-embedding-3-small";
    private static final int DIMENSIONS = 512;

    private final RestTemplate restTemplate;

    /**
     * 텍스트를 512차원 벡터로 임베딩
     *
     * @param text 임베딩할 텍스트
     * @return 512차원 float 배열
     */
    public float[] embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "input", text,
                "dimensions", DIMENSIONS
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    EMBEDDING_URL, HttpMethod.POST, request, Map.class
            );

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            List<Double> vector = (List<Double>) data.get(0).get("embedding");

            float[] result = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i).floatValue();
            }

            log.debug("임베딩 완료 - 차원: {}, 텍스트 길이: {}", result.length, text.length());
            return result;

        } catch (Exception e) {
            log.error("임베딩 실패 - 텍스트: {}...", text.substring(0, Math.min(50, text.length())), e);
            throw RagException.of(RagErrorCode.EMBEDDING_FAILED);
        }
    }
}