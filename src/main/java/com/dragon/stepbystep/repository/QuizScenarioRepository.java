package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.QuizScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizScenarioRepository extends JpaRepository<QuizScenario, Long> {
}