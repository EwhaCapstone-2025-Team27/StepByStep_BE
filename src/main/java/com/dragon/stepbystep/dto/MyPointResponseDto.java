package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyPointResponseDto {
    private String nickname;
    private Integer myPoint;
    private LocalDateTime updatedAt;

    public static MyPointResponseDto from(User user) {
        return new MyPointResponseDto(
                user.getNickname(),
                user.getPoints(),
                user.getPointsUpdatedAt()
        );
    }
}