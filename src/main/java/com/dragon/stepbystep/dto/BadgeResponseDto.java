package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.Badge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponseDto {
    private Long id;
    private String name;
    private String emoji;
    private String description;
    private Integer price;
    private boolean owned;

    public static BadgeResponseDto from(Badge badge, boolean owned) {
        return new BadgeResponseDto(
                badge.getId(),
                badge.getName(),
                badge.getEmoji(),
                badge.getDescription(),
                badge.getPrice(),
                owned
        );
    }
}