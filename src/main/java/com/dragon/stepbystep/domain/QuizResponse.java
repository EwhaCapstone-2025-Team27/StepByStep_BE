package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
@IdClass(QuizResponse.QuizResponseId.class)
public class QuizResponse {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(insertable = false, updatable = false)
    private QuizQuestion question;

    @ManyToOne(fetch = LAZY)
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
