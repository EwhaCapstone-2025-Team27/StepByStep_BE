// src/main/java/com/dragon/stepbystep/domain/QuizQuestion.java
package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stem", nullable = false, columnDefinition = "TEXT")
    private String stem;  // 질문 본문

    @Column(name = "correct_text", columnDefinition = "LONGTEXT")
    private String correctText;  // 해설

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private QuizScenario scenario;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizResponse> responses = new ArrayList<>();
}