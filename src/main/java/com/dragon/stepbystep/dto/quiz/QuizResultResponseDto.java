package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizResultResponseDto {
    private String resultId; // = quizId
    private int total;
    private int correctCount;
    private int earnedPointsTotal;
    private List<QuizResultItemDto> items;
}