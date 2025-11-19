// src/main/java/com/dragon/stepbystep/ai/AIClient.java
package com.dragon.stepbystep.ai;

import java.time.Duration;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIClient {

    private final WebClient.Builder webClientBuilder;

    // properties 파일의 설정 값들 사용
    @Value("${ai.base-url:http://127.0.0.1:8001}")
    private String aiBaseUrl;

    @Value("${ai.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${ai.response-timeout-ms:10000}")
    private int responseTimeoutMs;

    @Value("${ai.max-in-memory-mb:10}")
    private int maxInMemoryMb;

    private WebClient client() {
        log.debug("🔧 AI 클라이언트 생성: {}", aiBaseUrl);

        // HTTP 클라이언트 최적화 설정
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(responseTimeoutMs))  // properties에서 가져옴
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .option(ChannelOption.SO_KEEPALIVE, true)  // Keep-Alive 설정
                .option(ChannelOption.TCP_NODELAY, true);  // Nagle 알고리즘 비활성화

        return webClientBuilder
                .baseUrl(aiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(maxInMemoryMb * 1024 * 1024)) // properties에서 가져옴
                .build();
    }

    // -------------------- Chat Stream (초고속 연결) --------------------
    public Flux<String> chatStream(String json, String userId) {
        log.info("🔗 AI 서버 연결 시작: {}", aiBaseUrl);
        long startTime = System.currentTimeMillis();

        return client().post()
                .uri("/api/chat/stream") // 최종 경로: AI_BASE_URL + /api/chat/stream
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-User-Id", userId)
                .bodyValue(json)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(token -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < 1000) {  // 첫 1초 동안만 상세 로깅
                        log.debug(" 첫 토큰 수신 ({}ms): {}", elapsed,
                                token.length() > 20 ? token.substring(0, 20) + "..." : token);
                    }
                })
                .doOnError(error -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.error(" AI 연결 실패 ({}ms): {}", elapsed, error.getMessage());
                })
                .doOnComplete(() -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.info(" AI 스트리밍 완료: {}ms", elapsed);
                });
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