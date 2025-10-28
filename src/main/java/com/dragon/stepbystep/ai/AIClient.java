package com.dragon.stepbystep.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url:http://127.0.0.1:8000}")
    private String aiBaseUrl;

    private WebClient client() {
        return webClientBuilder.baseUrl(aiBaseUrl).build();
    }

    // ---------- Chat ----------
    public String chat(String json, String userId) {
        try {
            return client().post()
                    .uri("/v1/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", userId) // 내부 인증(로컬/사설망)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI chat error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    // ---------- Quiz ----------
    public String quizKeywords(String q, Integer limit, String userId) {
        try {
            return client().get()
                    .uri(uri -> uri.path("/api/quiz/keywords")
                            .queryParamIfPresent("q", (q == null || q.isBlank()) ? java.util.Optional.empty() : java.util.Optional.of(q))
                            .queryParamIfPresent("limit", limit == null ? java.util.Optional.empty() : java.util.Optional.of(limit))
                            .build())
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI keywords error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public String createQuiz(String mode, String keyword, Integer n, String userId) {
        try {
            return client().get()
                    .uri(uri -> uri.path("/api/quiz")
                            .queryParam("mode", mode)
                            .queryParamIfPresent("keyword", (keyword == null || keyword.isBlank()) ? java.util.Optional.empty() : java.util.Optional.of(keyword))
                            .queryParamIfPresent("n", n == null ? java.util.Optional.empty() : java.util.Optional.of(n))
                            .build())
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("AI createQuiz error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
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
            log.warn("AI submitAnswer error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
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
            log.warn("AI getResult error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
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
        log.warn("AI moderation error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
        throw e;
    }
}
// 배치 텍스트 검사
public String moderationCheckBatch(String rawJson, String userId) {
    return client().post().uri("/api/moderation/check-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-Id", userId)
            .bodyValue(rawJson).retrieve().bodyToMono(String.class).block();
}

// RAG 스니펫 필터
public String moderationFilterSnippets(String rawJson, String userId) {
    return client().post().uri("/api/moderation/filter-snippets")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-Id", userId)
            .bodyValue(rawJson).retrieve().bodyToMono(String.class).block();
}

// 혼합 배치 가드
public String moderationGuardBatch(String rawJson, String userId) {
    return client().post().uri("/api/moderation/guard-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-Id", userId)
            .bodyValue(rawJson).retrieve().bodyToMono(String.class).block();
}

// -------------------- Chat Stream --------------------
import reactor.core.publisher.Flux;

public Flux<String> chatStream(String json, String userId) {
    return client().post()
            .uri("/api/chat/stream") // AI 서버의 SSE endpoint
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header("X-User-Id", userId)
            .bodyValue(json)
            .retrieve()
            .bodyToFlux(String.class); // SSE 라인 그대로 전달
}