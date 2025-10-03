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

    @NotBlank
    private String nickname;

    @NotNull
    private GenderType gender;

    @NotNull
    private Integer birthyear;
}