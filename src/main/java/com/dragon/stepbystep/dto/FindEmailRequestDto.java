package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class FindEmailRequestDto {

    String nickname;

    GenderType gender;

    Integer birthyear;
}
