package com.dragon.stepbystep.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizGetResponseDto {
    private String quizId;  // attempt.id
    private String mode;
    private String keyword;
    private Integer total;
    private List<QuizItemDto> items;
}
