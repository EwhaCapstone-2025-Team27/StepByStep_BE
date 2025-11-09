package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.QuizResponse;
import com.dragon.stepbystep.domain.QuizResponseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResponseRepository extends JpaRepository<QuizResponse, QuizResponseId> {
    List<QuizResponse> findByAttemptId(Long attemptId);
    Optional<QuizResponse> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
