// src/main/java/com/dragon/stepbystep/ai/AIClient.java
package com.dragon.stepbystep.ai;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIClient {

    private final WebClient.Builder webClientBuilder;

    // FastAPI AI 서버 베이스 URL (예: http://127.0.0.1:8000)
    @Value("${ai.base-url:http://127.0.0.1:8000}")
    private String aiBaseUrl;

    private WebClient client() {
        return webClientBuilder.baseUrl(aiBaseUrl).build();
    }

    // -------------------- Chat Stream (명세: POST /api/chat/stream) --------------------
    public Flux<String> chatStream(String json, String userId) {
        return client().post()
                .uri("/api/chat/stream") // 최종 경로: AI_BASE_URL + /api/chat/stream
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-User-Id", userId)
                .bodyValue(json)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(error -> log.warn("AI chat stream error", error));
    }

    // ---------- Quiz ----------
    public String quizKeywords(String q, Integer limit, String userId) {
        try {
            return client().get()
                    .uri(uri -> uri.path("/api/quiz/keywords")
                            .queryParamIfPresent("q", Optional.ofNullable(q).filter(s -> !s.isBlank()))
                            .queryParamIfPresent("limit", Optional.ofNullable(limit))
                            .build())
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI keywords error [{}] body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public String createQuiz(String mode, String keyword, Integer n, String userId) {
        try {
            return client().get()
                    .uri(uri -> uri.path("/api/quiz")
                            .queryParam("mode", mode)
                            .queryParamIfPresent("keyword", Optional.ofNullable(keyword).filter(s -> !s.isBlank()))
                            .queryParamIfPresent("n", Optional.ofNullable(n))
                            .build())
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI createQuiz error [{}] body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public String submitAnswer(String rawJson, String userId) {
        try {
            return client().post()
                    .uri("/api/quiz/answer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", userId)
                    .bodyValue(rawJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI submitAnswer error [{}] body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public String getResult(String resultId, String userId) {
        try {
            return client().get()
                    .uri("/api/quiz/results/{id}", resultId)
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI getResult error [{}] body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    // ---------- Moderation ----------
    public String moderation(String path, String json, String userId) {
        try {
            return client().post()
                    .uri("/api/moderation" + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", userId)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI moderation error [{}] body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public String moderationCheckBatch(String rawJson, String userId) {
        return moderation("/check-batch", rawJson, userId);
    }

    public String moderationFilterSnippets(String rawJson, String userId) {
        return moderation("/filter-snippets", rawJson, userId);
    }

    public String moderationGuardBatch(String rawJson, String userId) {
        return moderation("/guard-batch", rawJson, userId);
    }

    public String moderationCheck(String rawJson, String userId) {
        return moderation("/check", rawJson, userId);
    }

    public String moderationGuardInput(String rawJson, String userId) {
        return moderation("/guard-input", rawJson, userId);
    }

    public String moderationGuardOutput(String rawJson, String userId) {
        return moderation("/guard-output", rawJson, userId);
    }

    public String moderationGuardPost(String rawJson, String userId) {
        return moderation("/guard-post", rawJson, userId);
    }

    public String moderationGuardComment(String rawJson, String userId) {
        return moderation("/guard-comment", rawJson, userId);
    }
}