package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {

    private final AIClient ai;
    private final ObjectMapper om;

    private String userId(Authentication auth) {
        return (auth != null && auth.getName() != null) ? auth.getName() : "0";
    }

    @GetMapping("/keywords")
    public ResponseEntity<ApiResponse<JsonNode>> keywords(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer limit,
            Authentication auth
    ) throws Exception {
        String raw = ai.quizKeywords(q, limit, userId(auth));
        return ResponseEntity.ok(ApiResponse.success("퀴즈 키워드 조회 성공!", om.readTree(raw)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> create(
            @RequestParam String mode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer n,
            Authentication auth
    ) throws Exception {
        String raw = ai.createQuiz(mode, keyword, n, userId(auth));
        return ResponseEntity.ok(ApiResponse.success("퀴즈 생성 성공!", om.readTree(raw)));
    }

    @PostMapping("/answer")
    public ResponseEntity<ApiResponse<JsonNode>> answer(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.submitAnswer(body.toString(), userId(auth));
        return ResponseEntity.ok(ApiResponse.success("퀴즈 답안 제출 성공!", om.readTree(raw)));
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<ApiResponse<JsonNode>> result(@PathVariable String id, Authentication auth) throws Exception {
        String raw = ai.getResult(id, userId(auth));
        return ResponseEntity.ok(ApiResponse.success("퀴즈 결과 조회 성공!", om.readTree(raw)));
    }
}