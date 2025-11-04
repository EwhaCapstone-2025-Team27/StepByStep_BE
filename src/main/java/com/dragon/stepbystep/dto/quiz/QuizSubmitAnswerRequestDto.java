package com.dragon.stepbystep.dto.quiz;

import lombok.Data;

@Data
public class QuizSubmitAnswerRequestDto {
    private String quizId;
    private String itemId;
    private int choiceIndex; // 0~3
}