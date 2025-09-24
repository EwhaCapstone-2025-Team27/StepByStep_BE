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

    private String email;

    private String password;

    private String nickname;

    private String gender;

    private int birthyear;

    public User toEntity(String encodedPassword){
        User user = new User();
        user.setEmail(this.email);
        user.setPasswordHash(this.password);
        user.setNickname(this.nickname);
        user.setGender(GenderType.valueOf(gender));
        user.setBirthyear(this.birthyear);
        return user;
    }
}
