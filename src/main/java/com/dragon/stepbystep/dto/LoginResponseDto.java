package com.dragon.stepbystep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NotBlank
@Builder
public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    private boolean forcePasswordChange;
}
