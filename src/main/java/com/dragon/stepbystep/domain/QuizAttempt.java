package com.dragon.stepbystep.domain;

import com.dragon.stepbystep.domain.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table
public class QuizAttempt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private QuizScenario scenario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private User user;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime submittedAt;

    private Integer scoreTotal;
    private Integer scoreMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;
}
