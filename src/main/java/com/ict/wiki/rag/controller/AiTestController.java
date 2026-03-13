package com.ict.wiki.rag.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/test")
@RequiredArgsConstructor
public class AiTestController {

    private final ChatClient.Builder chatClientBuilder;
    private final EmbeddingModel embeddingModel;

    /**
     * GPT 호출 테스트
     * GET /api/ai/test/chat?message=안녕
     */
    @GetMapping("/chat")
    public Map<String, String> testChat(@RequestParam String message) {
        String response = chatClientBuilder.build()
                .prompt()
                .user(message)
                .call()
                .content();

        return Map.of("response", response);
    }

    /**
     * 임베딩 테스트
     * GET /api/ai/test/embed?text=호천관 303호 빔프로젝터 안나옴
     */
    @GetMapping("/embed")
    public Map<String, Object> testEmbed(@RequestParam String text) {
        float[] embedding = embeddingModel.embed(text);

        return Map.of(
                "text", text,
                "dimensions", embedding.length,
                "first5", List.of(
                        embedding[0], embedding[1], embedding[2],
                        embedding[3], embedding[4]
                )
        );
    }

    @PostMapping("/embed")
    public Map<String, Object> testEmbedPost(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        float[] embedding = embeddingModel.embed(text);

        return Map.of(
                "text", text,
                "dimensions", embedding.length,
                "vector", embedding
        );
    }
}