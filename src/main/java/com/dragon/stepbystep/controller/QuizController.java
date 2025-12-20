package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // 키워드 목록
    @GetMapping({"/keywords"})
    public ResponseEntity<List<QuizScenarioDto>> getKeywords() {
        return ResponseEntity.ok(quizService.getKeywords());
    }

    // 퀴즈 세트 생성 (2문제가 한 세트)
    @PostMapping("/attempts")
    public ResponseEntity<QuizAttemptCreateResponseDto> createAttempt(
            @RequestBody QuizAttemptCreateRequestDto request,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "인증이 필요합니다."
            );
        }
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(quizService.createAttempt(request, userId));
    }

    // 문항별 보기 선택 및 제출 (답변 기록 + 채점)
    @PostMapping("/attempts/{attemptId}/responses")
    public ResponseEntity<QuizAnswerSubmitResponseDto> submitResponse(
            @PathVariable Long attemptId,
            @RequestBody QuizAnswerSubmitRequestDto request
    ) {
        return ResponseEntity.ok(quizService.submitResponse(attemptId, request));
    }

    // 퀴즈 결과 조회
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizAttemptResultResponseDto> getAttemptResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizService.getAttemptResult(attemptId));
    }
}