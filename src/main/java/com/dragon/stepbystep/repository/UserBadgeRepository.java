package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);
}