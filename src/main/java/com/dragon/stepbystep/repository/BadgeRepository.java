package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.Badge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findAllBy(Pageable pageable);
    List<Badge> findByIdLessThan(Long id, Pageable pageable);
}