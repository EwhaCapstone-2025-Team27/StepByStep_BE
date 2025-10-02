package com.dragon.stepbystep.domain;

import com.dragon.stepbystep.domain.enums.GenderType;
import com.dragon.stepbystep.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id; // BIGINT UNSIGNED AUTO_INCREMENT

    @Column(name = "login_id", nullable = false, length = 20, unique = true)
    private String loginId; // lowercase only (DB CHECK), service/DTO에서 추가 검증 권장

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash; // BCrypt(60)

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


    // 편의 메서드
    public void softDelete() {
        this.status = UserStatus.DELETED;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }
}