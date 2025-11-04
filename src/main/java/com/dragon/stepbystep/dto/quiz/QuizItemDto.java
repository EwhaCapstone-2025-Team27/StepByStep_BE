package com.dragon.stepbystep.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class QuizItemDto {
    private String itemId;
    private String type;              // "situation" | "concept"
    private String question;
    private List<String> choices;     // 4개
    private List<Map<String, Object>> references; // {"source": "...", "chunk_id": 123} 등
}