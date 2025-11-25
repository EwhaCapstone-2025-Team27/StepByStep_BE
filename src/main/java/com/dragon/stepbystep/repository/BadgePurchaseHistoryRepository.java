package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.BadgePurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgePurchaseHistoryRepository extends JpaRepository<BadgePurchaseHistory, Long> {
}