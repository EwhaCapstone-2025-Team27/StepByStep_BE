package com.dragon.stepbystep.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "token_blacklist",
        indexes = {
                @Index(name = "idx_token_blacklist_expires", columnList = "expires_at")
        })
public class TokenBlacklist {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;


    @Column(name = "token_hash", nullable = false, length = 128, unique = true)
    private String tokenHash; // Access/Refresh 토큰 해시


    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt; // 토큰 만료 시각과 동일하게 저장
}