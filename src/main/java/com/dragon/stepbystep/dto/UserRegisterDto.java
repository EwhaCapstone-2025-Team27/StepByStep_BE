package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 10, message = "닉네임은 2~10자")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣A-Za-z0-9]+$", message = "한글/영문/숫자만 허용")
    private String nickname;

    @NotBlank
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자")
    private String password;

    @NotBlank
    private String passwordConfirm;

    @NotNull
    private GenderType gender;

    @JsonProperty("birthYear")
    @NotNull
    @Min(value = 1900, message = "1900 이상")
    @Max(value = 2100, message = "2100 이하")
    private Integer birthyear;

    public User toEntity(String encodedPassword) {
        User user = new User();
        user.setEmail(this.email);
        user.setNickname(this.nickname);
        user.setPasswordHash(encodedPassword);
        user.setGender(this.gender);
        user.setBirthyear(this.birthyear);
        return user;
    }
}
