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
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20, nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderType gender;

    @Column(nullable = false)
    private Integer birthyear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    public void patch(User dto) {
        if (dto.getEmail() != null && !dto.getEmail().equals(this.email)) {
            throw new IllegalArgumentException("이메일은 수정할 수 없습니다.");
        }
        if (dto.getPassword() != null) {
            this.password = dto.getPassword();
        }
        if (dto.getNickname() != null) {
            this.nickname = dto.getNickname();
        }
        if (dto.getGender() != null) {
            this.gender = dto.getGender();
        }
        if (dto.getBirthyear() > 0) {
            this.birthyear = dto.getBirthyear();
        }

    }
}