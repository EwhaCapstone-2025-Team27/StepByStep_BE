package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.enums.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindEmailRequestDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotNull(message = "성별을 선택해주세요.")
    private GenderType gender;

    @NotNull(message = "출생년도를 입력해주세요.")
    private Integer birthyear;
}
