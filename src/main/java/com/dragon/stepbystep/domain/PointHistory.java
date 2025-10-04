package com.dragon.stepbystep.domain;

import com.dragon.stepbystep.domain.enums.PointHistoryType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "point_histories")
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private PointHistoryType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "point_change", nullable = false)
    private Integer pointChange;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;
}