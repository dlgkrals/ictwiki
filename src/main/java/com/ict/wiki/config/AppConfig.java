package com.ict.wiki.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

/**
 * 애플리케이션 공통 설정
 * - RestTemplate: OpenAI API 호출용 (EmbeddingService, SimilarCaseService)
 * - @EnableAsync: RagEventHandler의 @Async 비동기 처리 활성화
 */
@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}