package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionResponseDto {
    private Integer index;
    private Long questionId;
    private String stem;
    private List<QuizOptionResponseDto> options;
}