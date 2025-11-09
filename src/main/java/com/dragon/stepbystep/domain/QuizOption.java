package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;  // 선택지 내용

    @Column(name = "label", nullable = false, length = 5)
    private String label;  // A, B, C, D

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;
}