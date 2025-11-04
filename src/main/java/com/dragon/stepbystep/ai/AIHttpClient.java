package com.dragon.stepbystep.ai;

import com.dragon.stepbystep.dto.quiz.QuizKeywordDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class AIHttpClient implements AIClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${ai.base-url:http://127.0.0.1:8000}")
    private String aiBaseUrl;

    private WebClient client() {
        return webClientBuilder.baseUrl(aiBaseUrl).build();
    }

    // ---------- Quiz: 키워드 목록 ----------
    @Override
    public List<QuizKeywordDto> fetchQuizKeywords(String q, int limit) {
        try {
            String json = client().get()
                    .uri(uri -> uri.path("/api/quiz/keywords")
                            .queryParam("q", (q == null || q.isBlank()) ? null : q)
                            .queryParam("limit", limit > 0 ? limit : null)
                            .build())
                    .header("X-User-Id", "0") // 게이트웨이/사설망 보호 전제
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            KeywordsResp resp = om.readValue(json, KeywordsResp.class);
            if (resp.items == null) return List.of();

            List<QuizKeywordDto> out = new ArrayList<>();
            for (KeywordItem it : resp.items) {
                out.add(new QuizKeywordDto(
                        it.key,
                        it.label,
                        it.sampleTopics == null ? List.of() : it.sampleTopics
                ));
            }
            return out;

        } catch (WebClientResponseException e) {
            log.warn("AI keywords error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse keywords", e);
        }
    }

    // ---------- Quiz: 세트 생성 ----------
    @Override
    public QuizBundle generateQuiz(String mode, String keyword, int n, Integer seed) {
        try {
            String json = client().get()
                    .uri(uri -> uri.path("/api/quiz")
                            .queryParam("mode", mode)
                            .queryParam("keyword", (keyword == null || keyword.isBlank()) ? null : keyword)
                            .queryParam("n", n > 0 ? n : null)
                            .queryParam("seed", seed)
                            .build())
                    .header("X-User-Id", "0")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            QuizResp resp = om.readValue(json, QuizResp.class);

            List<AIClient.QuizItem> items = new ArrayList<>();
            if (resp.items != null) {
                for (QuizItemResp it : resp.items) {
                    items.add(AIClient.QuizItem.builder()
                            .type(it.type)
                            .question(it.question)
                            .choices(it.choices == null ? List.of() : it.choices)
                            .references(it.references == null ? List.of() : it.references)
                            .rationale(null)
                            .answerIndex(-1) // 정답은 내려오지 않음
                            .build());
                }
            }

            return AIClient.QuizBundle.builder()
                    .mode(resp.mode)
                    .keyword(resp.keyword)
                    .items(items)
                    .build();

        } catch (WebClientResponseException e) {
            log.warn("AI createQuiz error [{}] body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse quiz response", e);
        }
    }

    // ---------- 내부 파서용 DTO ----------
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KeywordsResp { public List<KeywordItem> items; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KeywordItem {
        public String key;
        public String label;
        public List<String> sampleTopics;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QuizResp {
        public String mode;
        public String keyword;
        public String quizId; // 필요 시 컨트롤러에서 사용 가능
        public List<QuizItemResp> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QuizItemResp {
        public String type;
        public String question;
        public List<String> choices;
        public List<Map<String, Object>> references;
    }
}