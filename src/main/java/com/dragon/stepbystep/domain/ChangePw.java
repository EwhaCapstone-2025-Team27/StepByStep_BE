package com.dragon.stepbystep.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePw {


    private String currentPassword;

    @NotEmpty(message = "새 비밀번호는 필수항목입니다.")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인은 필수항목입니다.")
    private String newPasswordConfirm;
}
