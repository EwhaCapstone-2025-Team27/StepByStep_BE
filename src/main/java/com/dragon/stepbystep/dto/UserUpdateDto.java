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
public class UserUpdateDto {

    private String nickname;

    private String gender;

    private int birthyear;

    public User toEntity(){
        User user = new User();
        user.setEmail(null);
        user.setPasswordHash(null);
        user.setNickname(this.nickname);
        user.setGender(GenderType.valueOf(gender));
        user.setBirthyear(this.birthyear);
        return user;
    }
}
