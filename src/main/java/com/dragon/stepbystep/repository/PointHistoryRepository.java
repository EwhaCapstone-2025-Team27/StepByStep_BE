package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.PointHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByUserId(Long userId, Pageable pageable);
    List<PointHistory> findByUserIdAndIdLessThan(Long userId, Long id, Pageable pageable);
}