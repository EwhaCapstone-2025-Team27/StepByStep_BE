package com.dragon.stepbystep.domain;

import com.dragon.stepbystep.domain.enums.GenderType;
import com.dragon.stepbystep.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id; // BIGINT UNSIGNED AUTO_INCREMENT

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "temp_password_hash", length = 60)
    private String tempPasswordHash;

    @Column(name = "temp_password_expires_at")
    private Instant tempPasswordExpiresAt;

    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;

    @Column(name = "nickname", nullable = false, length = 50, unique = true)
    private String nickname; // 3~10자 한/영/숫자 (서비스/DTO 검증)

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, columnDefinition = "ENUM('F','M')")
    private GenderType gender;

    @Column(name = "birthyear", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer birthyear; // 1900~현재년 (DB는 2100 체크, 서비스에서 범위 보강)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;


    // 도메인 규칙 메서드
    public void softDelete() { this.status = UserStatus.DELETED; }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        // 임시 비번 상태 해제
        this.tempPasswordHash = null;
        this.tempPasswordExpiresAt = null;
        this.mustChangePassword = false;
    }

    public void issueTemporaryPassword(String tempHash, Instant expiresAt) {
        this.tempPasswordHash = tempHash;
        this.tempPasswordExpiresAt = expiresAt;
        this.mustChangePassword = true;
    }

    public boolean isTempPasswordValidNow(Instant now) {
        return this.tempPasswordHash != null && this.tempPasswordExpiresAt != null && now.isBefore(this.tempPasswordExpiresAt);
    }
}