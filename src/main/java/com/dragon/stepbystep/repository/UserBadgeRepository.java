package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.UserBadge;
import com.dragon.stepbystep.dto.UserBadgeResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    @Query("select ub.badge.id from UserBadge ub where ub.user.id = :userId")
    List<Long> findBadgeIdsByUserId(@Param("userId") Long userId);

    @Query("""
            select new com.dragon.stepbystep.dto.UserBadgeResponseDto(
                ub.badge.id,
                ub.badge.name,
                ub.badge.emoji,
                ub.badge.description,
                ub.priceAtPurchase,
                ub.createdAt
            )
            from UserBadge ub
            where ub.user.id = :userId
            order by ub.createdAt desc
            """)
    List<UserBadgeResponseDto> findBadgeResponsesByUserId(@Param("userId") Long userId);
}