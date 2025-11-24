package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponseItemDto {
    private Long questionId;
    private String stem;
    private Long selectedOptionId;
    private String selectedOptionLabel;
    private String selectedOptionText;
    private Boolean isCorrect;
    private Long correctOptionId;
    private String correctOptionLabel;
    private String correctOptionText;
    private String explanation;
}