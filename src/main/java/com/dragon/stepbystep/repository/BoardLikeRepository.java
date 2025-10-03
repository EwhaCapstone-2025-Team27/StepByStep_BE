package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    boolean existsByBoard_IdAndUser_Id(Long boardId, Long userId);
    Optional<BoardLike> findByBoard_IdAndUser_Id(Long boardId, Long userId);
}