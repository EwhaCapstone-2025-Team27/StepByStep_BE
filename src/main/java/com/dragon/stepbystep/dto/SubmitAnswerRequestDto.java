package com.dragon.stepbystep.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequestDto {
    private String quizId;      // attempt.id
    private String itemId;      // question.id
    private Integer choiceIndex;  // 0-3
}
