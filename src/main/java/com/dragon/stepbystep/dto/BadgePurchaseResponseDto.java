package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.Badge;
import com.dragon.stepbystep.domain.UserBadge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BadgePurchaseResponseDto {

    private Long id;
    private BadgeInfo badge;
    private PointsChange points;
    private boolean owned;
    private LocalDateTime purchasedAt;

    public static BadgePurchaseResponseDto of(UserBadge userBadge, int before, int used, int after) {
        Badge badge = userBadge.getBadge();
        return new BadgePurchaseResponseDto(
                userBadge.getId(),
                new BadgeInfo(
                        badge.getId(),
                        badge.getName(),
                        badge.getEmoji(),
                        badge.getDescription(),
                        badge.getPrice()
                ),
                new PointsChange(before, used, after),
                true,
                userBadge.getPurchasedAt()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BadgeInfo {
        private Long badgeId;
        private String name;
        private String emoji;
        private String description;
        private Integer price;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointsChange {
        private Integer before;
        private Integer used;
        private Integer after;
    }
}