package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.Badge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByIsActiveTrue(Pageable pageable);
    List<Badge> findByIsActiveTrueAndIdLessThan(Long id, Pageable pageable);
    Optional<Badge> findByIdAndIsActiveTrue(Long id);
}