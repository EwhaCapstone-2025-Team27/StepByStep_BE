package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.service.QuizRewardService;
import com.dragon.stepbystep.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizRewardService quizRewardService;

    // 추가!
    private Long userId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return 0L;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // 1. 퀴즈 생성
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QuizGetResponseDto>> generate(
            @RequestBody QuizGenerateRequestDto body,
            Authentication auth
    ) {
        QuizGetResponseDto response = quizService.generateQuiz(
                body.getKeyword(),
                body.getCount(),
                userId(auth)
        );

        return ResponseEntity.ok(ApiResponse.success("퀴즈 생성 성공!", response));
    }

    // 2. 답안 제출
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

    // 3. 결과 조회
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

    // 4. 사용자 퀴즈 히스토리 조회
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