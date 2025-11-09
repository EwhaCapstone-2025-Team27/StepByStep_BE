package com.dragon.stepbystep.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseId {
    private Long attempt;
    private Long question;
}
