package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table
public class ChatAnswer {
    @Id
    @Column
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn
    private ChatMessage message;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
