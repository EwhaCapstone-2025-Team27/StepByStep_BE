package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
            message = "닉네임은 2~10자의 한글/영문/숫자만 가능합니다.")
    private String nickname;

    private GenderType gender;

    private Integer birthyear;

    public User toEntity(){
        User user = new User();
        user.setNickname(this.nickname);
        user.setGender(this.gender);
        user.setBirthyear(this.birthyear);
        return user;
    }
}
