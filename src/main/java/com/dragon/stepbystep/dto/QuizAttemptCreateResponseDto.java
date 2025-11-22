package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptCreateResponseDto {
    private Long attemptId;
    private QuizScenarioDto scenario;
    private Integer questionCount;
    private List<QuizQuestionResponseDto> questions;
}