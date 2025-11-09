package com.dragon.stepbystep.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizItemDto {
    private String itemId;  // question.id
    private String type;    // "situation" | "concept"
    private String question;
    private List<String> choices;
    private List<Map<String, String>> references;
}
