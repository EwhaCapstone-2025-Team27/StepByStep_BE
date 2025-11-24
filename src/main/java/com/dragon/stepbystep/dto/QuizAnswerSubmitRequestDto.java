package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerSubmitRequestDto {
    private Long questionId;
    private Long optionId;
}