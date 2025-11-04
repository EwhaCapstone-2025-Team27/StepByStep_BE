package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizKeywordDto {
    private String key;
    private String label;
    private List<String> sampleTopics;
}