package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizKeywordListResponseDto {
    private List<QuizKeywordDto> items;
}