package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResultResponseDto {
    private Long attemptId;
    private QuizScenarioDto scenario;
    private QuizAttempt.QuizMode mode;
    private Integer scoreTotal;
    private Integer scoreMax;
    private QuizAttempt.AttemptStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private List<QuizAttemptResponseItemDto> responses;
}