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

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 3, max = 10, message = "닉네임은 3~10자여야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣A-Za-z0-9]+$", message = "닉네임은 한국어, 영어, 숫자만 사용할 수 있습니다.")
    private String nickname;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotNull(message = "성별을 선택해주세요.")
    private GenderType gender;

    @JsonProperty("birthYear")
    @NotNull(message = "출생년도를 입력해주세요.")
    @Min(value = 1900, message = "출생년도는 1900~2100 사이의 숫자만 가능합니다.")
    @Max(value = 2100, message = "출생년도는 1900~2100 사이의 숫자만 가능합니다.")
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
