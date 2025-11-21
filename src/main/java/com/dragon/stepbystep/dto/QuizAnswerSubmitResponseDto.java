package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerSubmitResponseDto {
    private Boolean correct;
    private Long correctOptionId;
    private String explanation;
    private Integer scoreDelta;
    private Integer totalScore;
    private Boolean finished;
}