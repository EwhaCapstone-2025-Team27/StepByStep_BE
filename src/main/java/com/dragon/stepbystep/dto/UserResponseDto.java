package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;

    private String email;

    private String nickname;

    private GenderType gender;

    private Integer birthyear;

    private Integer points;

    private List<UserBadgeResponseDto> badges;

    public static UserResponseDto fromEntity(User user) {
        return fromEntity(user, Collections.emptyList());
    }

    public static UserResponseDto fromEntity(User user, List<UserBadgeResponseDto> badges) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .birthyear(user.getBirthyear())
                .points(user.getPoints())
                .badges(badges)
                .build();
    }

}
