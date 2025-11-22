package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    @Query("select ub.badge.id from UserBadge ub where ub.user.id = :userId")
    List<Long> findBadgeIdsByUserId(@Param("userId") Long userId);

    @Query("select ub from UserBadge ub join fetch ub.badge where ub.user.id = :userId")
    List<UserBadge> findWithBadgeByUserId(@Param("userId") Long userId);
}