package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizSubmitAnswerResponseDto {
    private boolean correct;
    private int correctIndex;
    private String explanation;   // nullable
    private int earnedPoints;
    private Integer balance;      // optional
    private String resultId;      // = quizId
}