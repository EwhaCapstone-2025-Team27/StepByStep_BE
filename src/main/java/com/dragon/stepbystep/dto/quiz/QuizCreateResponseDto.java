package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizCreateResponseDto {
    private String quizId;
    private String mode;
    private String keyword; // nullable
    private int total;
    private List<QuizItemDto> items;
}