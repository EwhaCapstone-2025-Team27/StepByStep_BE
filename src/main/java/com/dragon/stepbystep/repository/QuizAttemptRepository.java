/* 포인트 구현시 수정 필요*/
package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
}