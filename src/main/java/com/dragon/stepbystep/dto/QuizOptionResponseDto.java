package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOptionResponseDto {
    private Long optionId;
    private String label;
    private String text;
}