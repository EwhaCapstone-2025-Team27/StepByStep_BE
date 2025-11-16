package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.QuizScenario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizScenarioRepository extends JpaRepository<QuizScenario, Long> {
    Optional<QuizScenario> findFirstByTitleContainingIgnoreCaseOrderByIdAsc(String keyword);

    List<QuizScenario> findByTitleContainingIgnoreCaseOrderByIdAsc(String keyword, Pageable pageable);

    List<QuizScenario> findAllByOrderByIdAsc(Pageable pageable);
}