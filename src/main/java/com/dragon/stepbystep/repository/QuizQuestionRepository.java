/* 포인트 구현시 수정 필요*/
package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> { }