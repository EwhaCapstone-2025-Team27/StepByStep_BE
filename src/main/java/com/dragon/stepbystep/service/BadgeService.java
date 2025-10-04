package com.dragon.stepbystep.service;

import com.dragon.stepbystep.common.CursorUtils;
import com.dragon.stepbystep.domain.Badge;
import com.dragon.stepbystep.domain.PointHistory;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.UserBadge;
import com.dragon.stepbystep.domain.enums.PointHistoryType;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.exception.BadgeAlreadyOwnedException;
import com.dragon.stepbystep.exception.BadgeNotFoundException;
import com.dragon.stepbystep.exception.InsufficientPointsException;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.BadgeRepository;
import com.dragon.stepbystep.repository.PointHistoryRepository;
import com.dragon.stepbystep.repository.UserBadgeRepository;
import com.dragon.stepbystep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public BadgeListResponseDto getBadges(Integer limitParam, String cursor) {
        int limit = normalizeLimit(limitParam);
        Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "id"));

        BadgeCursor badgeCursor = CursorUtils.decode(cursor, BadgeCursor.class);
        List<Badge> badges;
        if (badgeCursor != null && badgeCursor.lastId() != null) {
            badges = badgeRepository.findByIsActiveTrueAndIdLessThan(badgeCursor.lastId(), pageable);
        } else {
            badges = badgeRepository.findByIsActiveTrue(pageable);
        }

        boolean hasNext = badges.size() > limit;
        if (hasNext) {
            badges = badges.subList(0, limit);
        }

        String nextCursor = null;
        if (hasNext && !badges.isEmpty()) {
            Long lastId = badges.get(badges.size() - 1).getId();
            nextCursor = CursorUtils.encode(new BadgeCursor(lastId));
        }

        List<BadgeResponseDto> items = badges.stream()
                .map(BadgeResponseDto::from)
                .toList();

        return new BadgeListResponseDto(items, new CursorPagingDto(nextCursor, hasNext));
    }

    @Transactional
    public BadgePurchaseResponseDto purchaseBadge(Long userId, BadgePurchaseRequestDto requestDto) {
        if (requestDto == null || requestDto.id() == null) {
            throw new IllegalArgumentException("구매할 배지 ID는 필수입니다.");
        }

        Badge badge = badgeRepository.findByIdAndIsActiveTrue(requestDto.id())
                .orElseThrow(BadgeNotFoundException::new);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
            throw new BadgeAlreadyOwnedException();
        }

        int price = badge.getPrice();
        int before = user.getPoints();
        if (before < price) {
            throw new InsufficientPointsException();
        }

        user.decreasePoints(price);

        UserBadge userBadge = userBadgeRepository.save(
                UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .priceAtPurchase(price)
                        .build()
        );

        pointHistoryRepository.save(
                PointHistory.builder()
                        .user(user)
                        .type(PointHistoryType.SPEND)
                        .title("배지 구매: " + badge.getName())
                        .pointChange(-price)
                        .balanceAfter(user.getPoints())
                        .build()
        );

        return BadgePurchaseResponseDto.of(userBadge, before, price, user.getPoints());
    }

    private int normalizeLimit(Integer limitParam) {
        int limit = limitParam == null ? DEFAULT_LIMIT : limitParam;
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private record BadgeCursor(Long lastId) {
    }
}