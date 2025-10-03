package com.dragon.stepbystep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePwRequestDto {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 20) // 네 정책: 8~20자
    private String newPassword;

    @NotBlank
    private String newPasswordConfirm;
}
