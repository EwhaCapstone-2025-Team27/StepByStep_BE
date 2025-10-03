package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Page<Board> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Board> findByAuthor_NicknameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Board> findByContentContainingIgnoreCaseOrAuthor_NicknameContainingIgnoreCase(String contentKeyword, String nicknameKeyword, Pageable pageable);
}