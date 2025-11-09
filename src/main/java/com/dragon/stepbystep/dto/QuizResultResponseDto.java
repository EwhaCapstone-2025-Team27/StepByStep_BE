package com.dragon.stepbystep.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultResponseDto {
    private String resultId;
    private Integer total;
    private Integer correctCount;
    private Integer earnedPointsTotal;
    private List<ResultItemDto> items;
}