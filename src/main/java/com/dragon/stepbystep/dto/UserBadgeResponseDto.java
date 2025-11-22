package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.UserBadge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeResponseDto {
    private Long id;
    private String name;
    private String emoji;
    private String description;
    private Integer price;
    private LocalDateTime purchasedAt;

    public static UserBadgeResponseDto from(UserBadge userBadge) {
        return new UserBadgeResponseDto(
                userBadge.getBadge().getId(),
                userBadge.getBadge().getName(),
                userBadge.getBadge().getEmoji(),
                userBadge.getBadge().getDescription(),
                userBadge.getPriceAtPurchase(),
                userBadge.getPurchasedAt()
        );
    }
}