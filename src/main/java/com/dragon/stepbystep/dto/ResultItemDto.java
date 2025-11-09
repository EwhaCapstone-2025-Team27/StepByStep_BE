package com.dragon.stepbystep.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultItemDto {
    private String itemId;
    private Integer yourChoice;
    private Integer correctIndex;
    private Boolean correct;
    private Integer earnedPoints;
    private String question;
    private List<String> choices;
    private String explanation;
}