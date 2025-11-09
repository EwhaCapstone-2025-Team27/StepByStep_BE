package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderByLabel(Long questionId);
}