package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table
public class QuizOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private QuizQuestion question;

    @Column(length = 5, nullable = false)
    private String label; // A, B, C ...

    @Lob
    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Boolean isCorrect;
}
