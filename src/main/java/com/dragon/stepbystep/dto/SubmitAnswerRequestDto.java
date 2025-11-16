package com.dragon.stepbystep.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequestDto {
    private String quizId;      // attempt.id

    @JsonAlias("questionId")
    private String itemId;      // question.id

    @JsonAlias("choice")
    private Integer choiceIndex;  // 0-3

    private String keyword;     // optional context from FE
}