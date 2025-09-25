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

    private String currentPassword;

    private String newPassword;

    private String newPasswordConfirm;

    private String nickname;

    private GenderType gender;

    private Integer birthyear;

    public boolean wantsToChangePassword() {
        return currentPassword != null || newPassword != null || newPasswordConfirm != null;
    }

    public boolean hasAllRequiredFields() {
        return currentPassword != null && newPassword != null && newPasswordConfirm != null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public void validatePasswordInputsOrThrow(){
        if(!hasAllRequiredFields()){
            throw new IllegalArgumentException("비밀번호 변경에는 current/new/confirm 모두 필요합니다.");
        }

        if(isBlank(currentPassword) || isBlank(newPassword) || isBlank(newPasswordConfirm)){
            throw new IllegalArgumentException("비밀번호 입력값에 공백은 허용되지 않습니다.");
        }

        if(!newPassword.equals(newPasswordConfirm)){
            throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
        }

        if(newPassword.length()<8 || newPassword.length()>20){
            throw new IllegalArgumentException("비밀번호는 8~20자여야 합니다.");
        }

    }
    public User toEntity(){
        User user = new User();
        user.setEmail(null);
        user.setPassword(this.newPassword);
        user.setNickname(this.nickname);
        user.setGender(this.gender);
        user.setBirthyear(this.birthyear);
        return user;
    }
}
