package com.dragon.stepbystep.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAnswerResponseDto {
    private Boolean correct;
    private Integer correctIndex;
    private String explanation;
    private Integer earnedPoints;
    private Integer balance;
    private String resultId;  // quizId (=attempt.id)
}
