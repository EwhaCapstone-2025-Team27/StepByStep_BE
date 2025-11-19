package com.dragon.stepbystep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    @Value("${ai.base-url:http://127.0.0.1:8001}")
    private String aiBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * 퀴즈 세트 생성
     * - 실제 퀴즈 생성 + DB 저장은 전부 AI 서버(FastAPI)가 담당
     * - BE는 단순히 HTTP 프록시 역할만 수행
     */
    public Map<String, Object> createQuizSet(String mode, String keyword, int n, int userId) {

        // 1) 파라미터 1차 검증 (AI 서버와 동일한 정책)
        if (!"by_keyword".equals(mode) && !"random".equals(mode)) {
            throw new IllegalArgumentException("mode must be by_keyword|random");
        }
        if ("by_keyword".equals(mode) && (keyword == null || keyword.isBlank())) {
            throw new IllegalArgumentException("keyword required for by_keyword mode");
        }
        if (n < 1 || n > 10) {
            throw new IllegalArgumentException("n must be between 1 and 10");
        }

        try {
            // 2) URL 조립: GET /api/quiz?mode=...&n=...&user_id=...&keyword=...
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(aiBaseUrl + "/api/quiz")
                    .queryParam("mode", mode)
                    .queryParam("n", n)
                    .queryParam("user_id", userId);

            if (keyword != null && !keyword.isBlank()) {
                builder.queryParam("keyword", keyword);
            }

            URI uri = builder.build(true).toUri(); // true: 인코딩 유지

            log.info("[QuizService] 요청 → AI 서버 create_quiz: {}", uri);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            log.info("[QuizService] 퀴즈 생성 완료 (AI 응답 status={}): {}",
                    response.getStatusCode(), response.getBody());

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            // AI 서버가 던진 HTTP 에러를 조금 더 보기 좋게 래핑
            log.error("[QuizService] 퀴즈 생성 실패 - status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 퀴즈 생성 실패: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[QuizService] 퀴즈 생성 중 예외 발생", e);
            throw new RuntimeException("퀴즈 생성 중 오류 발생", e);
        }
    }

    /**
     * 키워드 추천 조회
     * - GET /api/quiz/keywords?q=...&limit=...
     */
    public Map<String, Object> getKeywords(String q, int limit) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(aiBaseUrl + "/api/quiz/keywords")
                    .queryParam("limit", limit);

            if (q != null && !q.isBlank()) {
                builder.queryParam("q", q);
            }

            URI uri = builder.build(true).toUri();

            log.info("[QuizService] 요청 → AI 서버 keywords: {}", uri);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("[QuizService] 키워드 조회 실패 - status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 키워드 조회 실패: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[QuizService] 키워드 조회 중 예외 발생", e);
            throw new RuntimeException("키워드 조회 중 오류 발생", e);
        }
    }

    /**
     * 답안 제출
     * - POST /api/quiz/answer
     * - request Body는 그대로 AI에게 전달하고, 응답도 그대로 FE에 전달
     */
    public Map<String, Object> submitAnswer(Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = aiBaseUrl + "/api/quiz/answer";
            log.info("[QuizService] 요청 → AI 서버 submitAnswer: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("[QuizService] 답안 제출 실패 - status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 답안 제출 실패: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[QuizService] 답안 제출 중 예외 발생", e);
            throw new RuntimeException("답안 제출 중 오류 발생", e);
        }
    }

    /**
     * 퀴즈 결과 조회
     * - GET /api/quiz/results/{resultId}
     */
    public Map<String, Object> getResults(String resultId) {
        try {
            String url = aiBaseUrl + "/api/quiz/results/" + resultId;
            log.info("[QuizService] 요청 → AI 서버 결과 조회: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("[QuizService] 결과 조회 실패 - status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 결과 조회 실패: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[QuizService] 결과 조회 중 예외 발생", e);
            throw new RuntimeException("결과 조회 중 오류 발생", e);
        }
    }
}