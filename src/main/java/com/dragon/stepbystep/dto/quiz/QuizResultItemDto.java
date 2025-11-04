package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizResultItemDto {
    private String itemId;
    private Integer yourChoice;  // -1 가능
    private int correctIndex;
    private boolean correct;
    private int earnedPoints;

    // 옵션(프런트에서 리뷰용으로 원하면)
    private String question;
    private List<String> choices;
    private String explanation;  // nullable
}