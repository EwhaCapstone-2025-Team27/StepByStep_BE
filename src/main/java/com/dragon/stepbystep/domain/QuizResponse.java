package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
@IdClass(QuizResponse.QuizResponseId.class)
public class QuizResponse {

    @Id
    @Column(name = "attempt_id")
    private Long attemptId;

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", insertable = false, updatable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QuizQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private QuizOption option;

    @Lob
    @Column
    private String textAnswer;

    @Column
    private Boolean isCorrect;

    private Integer score;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class QuizResponseId implements Serializable {
        private Long attemptId;
        private Long questionId;
    }
}