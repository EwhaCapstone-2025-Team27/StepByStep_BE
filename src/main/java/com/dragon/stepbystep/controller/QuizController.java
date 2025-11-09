package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 1. 퀴즈 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<QuizGetResponseDto> generateQuiz(  // ✅ Dto 추가
                                                             @RequestBody QuizGenerateRequestDto request,     // ✅ Dto 추가
                                                             @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        log.info("퀴즈 생성 요청: {}", request);

        if (userId == null) {
            userId = 1L;
        }

        QuizGetResponseDto response = quizService.generateQuiz(request, userId);  // ✅ Dto 추가
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 답안 제출
     */
    @PostMapping("/answer")
    public ResponseEntity<SubmitAnswerResponseDto> submitAnswer(  // ✅ Dto 추가
                                                                  @RequestBody SubmitAnswerRequestDto request  // ✅ Dto 추가
    ) {
        log.info("답안 제출: quizId={}, itemId={}, choiceIndex={}",
                request.getQuizId(), request.getItemId(), request.getChoiceIndex());

        SubmitAnswerResponseDto response = quizService.submitAnswer(request);  // ✅ Dto 추가
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 결과 조회
     */
    @GetMapping("/results/{resultId}")
    public ResponseEntity<QuizResultResponseDto> getResult(  // ✅ Dto 추가
                                                             @PathVariable String resultId
    ) {
        log.info("결과 조회: resultId={}", resultId);

        Long attemptId = Long.parseLong(resultId);
        QuizResultResponseDto response = quizService.getResult(attemptId);  // ✅ Dto 추가
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 사용자 퀴즈 히스토리 조회
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        if (userId == null) {
            userId = 1L;
        }

        log.info("퀴즈 히스토리 조회: userId={}", userId);

        return ResponseEntity.ok().body(Map.of("message", "구현 예정"));
    }
}