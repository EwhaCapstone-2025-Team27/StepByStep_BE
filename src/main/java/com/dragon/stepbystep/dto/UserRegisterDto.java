package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    private Long id;

    private String email;

    private String nickname;

    private String password;

    private String passwordConfirm;

    private GenderType gender;

    private Integer birthyear;

    public User toEntity(String encodedPassword) {
        User user = new User();
        user.setId(this.id);
        user.setEmail(this.email);
        user.setNickname(this.nickname);
        user.setPasswordHash(encodedPassword);
        user.setGender(this.gender);
        user.setBirthyear(this.birthyear);
        return user;
    }
}
