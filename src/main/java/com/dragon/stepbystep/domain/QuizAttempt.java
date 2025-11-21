package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private QuizScenario scenario;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    @Builder.Default
    private QuizMode mode = QuizMode.KEYWORD;

    @Column(name = "score_total")
    @Builder.Default
    private Integer scoreTotal = 0;  // 맞힌 개수

    @Column(name = "score_max")
    private Integer scoreMax;    // 전체 개수

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizResponse> responses = new ArrayList<>();

    public enum AttemptStatus {
        IN_PROGRESS, SUBMITTED, CANCELLED
    }

    public enum QuizMode {
        KEYWORD, RANDOM
    }
}