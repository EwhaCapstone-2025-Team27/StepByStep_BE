package com.dragon.stepbystep.ai;

import java.time.Duration;

import com.dragon.stepbystep.dto.ChatMessageRequest;
import com.dragon.stepbystep.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url:http://127.0.0.1:8000}")
    private String ragBaseUrl;

    @Value("${ai.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${ai.response-timeout-ms:10000}")
    private int responseTimeoutMs;

    @Value("${ai.max-in-memory-mb:10}")
    private int maxInMemoryMb;

    private WebClient client() {
        log.debug("RAG 클라이언트 생성: {}", ragBaseUrl);

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);

        return webClientBuilder
                .baseUrl(ragBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(maxInMemoryMb * 1024 * 1024))
                .build();
    }

    public ChatResponse chat(ChatMessageRequest body, String userId) {
        log.info("RAG 서버 /v1/chat 호출 시작: {}", ragBaseUrl);
        long startTime = System.currentTimeMillis();

        try {
            Mono<ChatResponse> responseMono = client().post()
                    .uri("/v1/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", userId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .doOnSuccess(res -> log.debug("RAG 응답 수신: {}ms", System.currentTimeMillis() - startTime));

            return responseMono.block();
        } catch (WebClientResponseException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("RAG 호출 실패 ({}ms): status={} body={}", elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

}