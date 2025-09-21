package com.dragon.stepbystep.domain;

import com.dragon.stepbystep.domain.BaseTimeEntity;
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
    private String passwordHash;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderType gender;

    @Column(nullable = false)
    private Integer birthyear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
}
