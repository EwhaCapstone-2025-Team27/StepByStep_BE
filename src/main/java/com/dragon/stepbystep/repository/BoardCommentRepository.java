package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findByBoard_IdOrderByCreatedAtAsc(Long boardId);
}