package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.dto.quiz.*;
import com.dragon.stepbystep.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // 1) 키워드 목록
    @GetMapping("/keywords")
    public ApiResponse<QuizKeywordListResponseDto> getKeywords(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ApiResponse.ok(quizService.getKeywords(q, limit, userId));
    }

    // 2) 퀴즈 세트 생성
    @GetMapping
    public ApiResponse<QuizCreateResponseDto> createQuiz(
            @RequestParam("mode") String mode,           // by_keyword | random
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "n", defaultValue = "5") int n,
            @RequestParam(value = "seed", required = false) Integer seed,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ApiResponse.ok(quizService.createQuiz(mode, keyword, n, seed, userId));
    }

    // 3) 보기 선택 및 제출
    @PostMapping("/answer")
    public ApiResponse<QuizSubmitAnswerResponseDto> submitAnswer(
            @RequestBody QuizSubmitAnswerRequestDto req,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ApiResponse.ok(quizService.submitAnswer(req, userId));
    }

    // 4) 결과 조회
    @GetMapping("/results/{resultId}")
    public ApiResponse<QuizResultResponseDto> getResult(
            @PathVariable("resultId") String resultId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ApiResponse.ok(quizService.getResult(resultId, userId));
    }
}