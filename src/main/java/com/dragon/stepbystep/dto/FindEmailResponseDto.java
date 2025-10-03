package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindEmailResponseDto {

    private Long id;

    private String email;

    public static FindEmailResponseDto fromEntity(User user) {
        return FindEmailResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
