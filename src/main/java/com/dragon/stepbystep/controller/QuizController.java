package com.dragon.stepbystep.controller;


import com.dragon.stepbystep.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 퀴즈 세트 생성 (AI 서버 호출)
     */
    @GetMapping
    public ResponseEntity<?> createQuiz(
            @RequestParam String mode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") int n,
            @RequestParam(defaultValue = "1") int userId
    ) {
        return ResponseEntity.ok(quizService.createQuizSet(mode, keyword, n, userId));
    }

    /**
     * 키워드 목록
     */
    @GetMapping("/keywords")
    public ResponseEntity<?> getKeywords(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(quizService.getKeywords(q, limit));
    }

    /**
     * 답안 제출
     */
    @PostMapping("/answer")
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(quizService.submitAnswer(request));
    }

    /**
     * 결과 조회
     */
    @GetMapping("/results/{resultId}")
    public ResponseEntity<?> getResults(@PathVariable String resultId) {
        return ResponseEntity.ok(quizService.getResults(resultId));
    }
}