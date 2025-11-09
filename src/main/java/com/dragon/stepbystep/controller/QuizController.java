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
    public ResponseEntity<QuizGetResponseDto> generateQuiz(
            @RequestBody QuizGenerateRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        log.info("퀴즈 생성 요청: keyword={}, count={}", request.getKeyword(), request.getCount());

        try {
            // keyword, count만 필요 (userId는 별도 처리)
            QuizGetResponseDto response = quizService.generateQuiz(
                    request.getKeyword(),
                    request.getCount() != null ? request.getCount() : 5
            );
            log.info(" 퀴즈 생성 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" 퀴즈 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 2. 답안 제출
     */
    @PostMapping("/answer")
    public ResponseEntity<SubmitAnswerResponseDto> submitAnswer(
            @RequestBody SubmitAnswerRequestDto request
    ) {
        log.info("답안 제출: quizId={}, itemId={}, choiceIndex={}",
                request.getQuizId(), request.getItemId(), request.getChoiceIndex());

        try {
            SubmitAnswerResponseDto response = quizService.submitAnswer(request);
            log.info(" 답안 제출 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" 답안 제출 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 3. 결과 조회
     */
    @GetMapping("/results/{resultId}")
    public ResponseEntity<QuizResultResponseDto> getResult(
            @PathVariable String resultId
    ) {
        log.info("결과 조회: resultId={}", resultId);

        try {
            Long attemptId = Long.parseLong(resultId);
            QuizResultResponseDto response = quizService.getResult(attemptId);
            log.info("결과 조회 성공");
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error(" 잘못된 resultId 형식: {}", resultId);
            return ResponseEntity.status(400).build();
        } catch (Exception e) {
            log.error(" 결과 조회 실패", e);
            return ResponseEntity.status(500).build();
        }
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