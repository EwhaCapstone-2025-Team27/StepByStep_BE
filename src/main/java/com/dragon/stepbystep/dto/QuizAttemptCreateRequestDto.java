package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptCreateRequestDto {
    private QuizAttempt.QuizMode mode;
    private Long scenarioId;
    private Integer questionCount;
}