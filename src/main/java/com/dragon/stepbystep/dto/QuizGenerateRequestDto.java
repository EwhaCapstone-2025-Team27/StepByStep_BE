package com.dragon.stepbystep.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerateRequestDto {
    private String mode;  // "by_keyword" | "random"
    private String keyword;
    private Integer count = 5;  // 문제 수
}