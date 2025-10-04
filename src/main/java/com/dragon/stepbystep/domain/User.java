package com.dragon.stepbystep.domain;

import com.dragon.stepbystep.domain.enums.GenderType;
import com.dragon.stepbystep.domain.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "nickname", nullable = false, length = 50, unique = true)
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{3,10}$",
            message = "닉네임은 3~10자의 한글, 영문, 숫자만 가능합니다.")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private GenderType gender;

    @Column(name = "birthyear", nullable = false)
    @Min(1900)
    private Integer birthyear; // 1900~현재년 (DB는 2100 체크, 서비스에서 범위 보강)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 0;

    @Column(name = "points_updated_at", nullable = false)
    private LocalDateTime pointsUpdatedAt;

    // 임시 비밀번호 관련 필드
    @Column(name = "temp_password_hash")
    private String tempPasswordHash;

    @Column(name = "temp_password_expires_at")
    private LocalDateTime tempPasswordExpiresAt;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion=0;

    private LocalDateTime tempPasswordIssuedAt;


    public void patch(User dto){

        // 닉네임, 성별, 출생년도 수정
        if(dto.getNickname() != null && !dto.getNickname().isBlank()) this.nickname = dto.getNickname();
        if(dto.getGender() != null) this.gender = dto.getGender();
        if(dto.getBirthyear() != null) this.birthyear = dto.getBirthyear();
    }

    public void incrementTokenVersion(){
        this.tokenVersion++;
    }

    public void markDeleted(){
        this.status = UserStatus.DELETED;
    }

    @PrePersist
    public void onPersist() {
        if (this.points == null) {
            this.points = 0;
        }
        if (this.pointsUpdatedAt == null) {
            this.pointsUpdatedAt = LocalDateTime.now();
        }
    }

    public void increasePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 증가는 음수가 될 수 없습니다.");
        }
        this.points += amount;
        this.pointsUpdatedAt = LocalDateTime.now();
    }

    public void decreasePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 차감은 음수가 될 수 없습니다.");
        }
        if (this.points < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.points -= amount;
        this.pointsUpdatedAt = LocalDateTime.now();
    }
}