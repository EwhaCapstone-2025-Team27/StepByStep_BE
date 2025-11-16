package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.*;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    // ===== Repositories =====
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizResponseRepository responseRepository;

    // ===== Repositories =====
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizResponseRepository responseRepository;
    private final QuizScenarioRepository scenarioRepository;

    private final Random random = new Random();

    // ===== Configuration Properties =====
    @Value("${quiz.points.correct:20}")
    private Integer pointsCorrect;

    @Value("${quiz.points.incorrect:0}")
    private Integer pointsIncorrect;

    @Value("${quiz.default.count:5}")
    private Integer defaultCount;

    @Value("${quiz.max.count:10}")
    private Integer maxCount;

    @Value("${quiz.min.count:1}")
    private Integer minCount;

    /**
     * 1. 퀴즈 생성 (RDS에 저장된 문제 기반)
     *
     * @param keyword 퀴즈 키워드
     * @param count 생성할 문제 수
     * @param userId 퀴즈를 생성하는 사용자
     * @return 데이터베이스에서 구성한 퀴즈 DTO
     */
    @Transactional
    public QuizGetResponseDto generateQuiz(String keyword, Integer count, Long userId) {
        int problemCount = normalizeCount(count);

        try {
            QuizScenario scenario = resolveScenario(keyword);

            List<QuizQuestion> questions = questionRepository
                    .findByScenarioIdOrderByIdAsc(scenario.getId());

            if (questions.isEmpty()) {
                throw new RuntimeException("선택한 시나리오에 등록된 문제가 없습니다.");
            }

            List<QuizQuestion> selectedQuestions = pickQuestions(questions, problemCount);

            QuizAttempt attempt = QuizAttempt.builder()
                    .userId(userId != null ? userId : 0L)
                    .scenario(scenario)
                    .scoreMax(selectedQuestions.size())
                    .scoreTotal(0)
                    .build();

            attemptRepository.save(attempt);

            List<QuizItemDto> items = selectedQuestions.stream()
                    .map(this::toQuizItemDto)
                    .collect(Collectors.toList());

            return QuizGetResponseDto.builder()
                    .quizId(String.valueOf(attempt.getId()))
                    .mode(StringUtils.hasText(keyword) ? "by_keyword" : "random")
                    .keyword(StringUtils.hasText(keyword) ? keyword : scenario.getTitle())
                    .total(items.size())
                    .items(items)
                    .build();
        } catch (Exception e) {
            log.error("퀴즈 생성 중 예외 발생", e);
            throw new RuntimeException("퀴즈 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 2. 답안 제출 및 채점
     *
     * @param request 답안 제출 요청
     * @return 채점 결과
     */
    @Transactional
    public SubmitAnswerResponseDto submitAnswer(SubmitAnswerRequestDto request) {
        Long attemptId = Long.parseLong(request.getQuizId());
        Long questionId = Long.parseLong(request.getItemId());
        Integer choiceIndex = request.getChoiceIndex();

        log.info("📝 답안 제출: attemptId={}, questionId={}, choiceIndex={}",
                attemptId, questionId, choiceIndex);

        try {
            // 중복 제출 체크 (멱등성)
            Optional<QuizResponse> existingResponse = responseRepository
                    .findByAttemptIdAndQuestionId(attemptId, questionId);

            if (existingResponse.isPresent()) {
                log.warn(" 중복 제출 감지: attemptId={}, questionId={}", attemptId, questionId);

                QuizResponse existing = existingResponse.get();
                Integer correctIndex = getCorrectIndex(questionId);
                String explanation = getExplanation(questionId);

                return SubmitAnswerResponseDto.builder()
                        .correct(existing.getIsCorrect())
                        .correctIndex(correctIndex)
                        .explanation(explanation != null ? explanation : "해설이 없습니다.")
                        .earnedPoints(0)  // 중복 제출은 포인트 없음
                        .balance(0)
                        .resultId(request.getQuizId())
                        .build();
            }

            // 정답 인덱스 조회
            Integer correctIndex = getCorrectIndex(questionId);
            boolean isCorrect = choiceIndex.equals(correctIndex);

            // 선택한 옵션 찾기
            QuizOption selectedOption = getOptionByIndex(questionId, choiceIndex);

            // 시도 및 문제 조회
            QuizAttempt attempt = attemptRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("QuizAttempt not found: " + attemptId));

            QuizQuestion question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("QuizQuestion not found: " + questionId));

            // 응답 저장
            QuizResponse response = QuizResponse.builder()
                    .attempt(attempt)
                    .question(question)
                    .option(selectedOption)
                    .isCorrect(isCorrect)
                    .score(isCorrect ? 1 : 0)
                    .createdAt(LocalDateTime.now())
                    .build();

            responseRepository.save(response);
            log.info(" 응답 저장 완료, correct={}", isCorrect);

            // 점수 업데이트
            if (isCorrect) {
                Integer currentScore = attempt.getScoreTotal() != null ? attempt.getScoreTotal() : 0;
                attempt.setScoreTotal(currentScore + 1);
                attemptRepository.save(attempt);
                log.info(" 정답 누적 점수={}", attempt.getScoreTotal());
            } else {
                log.info(" 오답: 선택={}, 정답={}", choiceIndex, correctIndex);
            }

            // 해설 조회
            String explanation = getExplanation(questionId);

            // 응답 반환
            return SubmitAnswerResponseDto.builder()
                    .correct(isCorrect)
                    .correctIndex(correctIndex)
                    .explanation(explanation != null ? explanation : "해설이 없습니다.")
                    .earnedPoints(isCorrect ? pointsCorrect : pointsIncorrect)
                    .balance(0)
                    .resultId(request.getQuizId())
                    .build();

        } catch (NumberFormatException e) {
            log.error(" 숫자 변환 오류: {}", e.getMessage());
            throw new RuntimeException("요청 형식이 잘못되었습니다.");
        } catch (Exception e) {
            log.error(" 답안 제출 중 예외 발생", e);
            throw new RuntimeException("답안 제출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 3. 결과 조회
     *
     * @param attemptId 시도 ID
     * @return 퀴즈 결과
     */
    @Transactional(readOnly = true)
    public QuizResultResponseDto getResult(Long attemptId) {
        try {
            log.info(" 결과 조회: attemptId={}", attemptId);

            QuizAttempt attempt = attemptRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("QuizAttempt not found: " + attemptId));

            List<QuizResponse> responses = responseRepository.findByAttemptId(attemptId);

            int correctCount = 0;
            List<ResultItemDto> items = new ArrayList<>();

            for (QuizResponse resp : responses) {
                Long questionId = resp.getQuestion().getId();
                Integer correctIndex = getCorrectIndex(questionId);
                Integer yourChoice = getOptionIndex(questionId,
                        resp.getOption() != null ? resp.getOption().getId() : null);

                if (Boolean.TRUE.equals(resp.getIsCorrect())) {
                    correctCount++;
                }

                List<String> choices = getChoices(questionId);

                ResultItemDto item = ResultItemDto.builder()
                        .itemId(String.valueOf(questionId))
                        .yourChoice(yourChoice)
                        .correctIndex(correctIndex)
                        .correct(resp.getIsCorrect())
                        .earnedPoints((resp.getIsCorrect() != null && resp.getIsCorrect())
                                ? pointsCorrect : pointsIncorrect)
                        .question(resp.getQuestion().getStem())
                        .choices(choices)
                        .explanation(resp.getQuestion().getCorrectText())
                        .build();

                items.add(item);
            }

            Integer totalPoints = correctCount * pointsCorrect;

            log.info(" 결과 조회 완료: total={}, correctCount={}, totalPoints={}",
                    items.size(), correctCount, totalPoints);

            return QuizResultResponseDto.builder()
                    .resultId(String.valueOf(attemptId))
                    .total(items.size())
                    .correctCount(correctCount)
                    .earnedPointsTotal(totalPoints)
                    .items(items)
                    .build();

        } catch (Exception e) {
            log.error(" 결과 조회 중 예외 발생", e);
            throw new RuntimeException("결과 조회 실패: " + e.getMessage(), e);
        }
    }

    // ===== Private Helper Methods =====

    // ===== Private Helper Methods =====

    private int normalizeCount(Integer requested) {
        int normalized = requested != null ? requested : defaultCount;
        if (normalized < minCount) {
            log.warn("요청된 count={} 이 min={} 보다 작아 보정합니다.", normalized, minCount);
            normalized = minCount;
        }
        if (normalized > maxCount) {
            log.warn("요청된 count={} 이 max={} 보다 커 보정합니다.", normalized, maxCount);
            normalized = maxCount;
        }
        return normalized;
    }

    private QuizScenario resolveScenario(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return scenarioRepository
                    .findFirstByTitleContainingIgnoreCaseOrderByIdAsc(keyword)
                    .orElseThrow(() -> new RuntimeException("해당 키워드와 일치하는 시나리오가 없습니다."));
        }

        List<QuizScenario> allScenarios = scenarioRepository.findAll();
        if (allScenarios.isEmpty()) {
            throw new RuntimeException("등록된 시나리오가 없습니다.");
        }
        return allScenarios.get(random.nextInt(allScenarios.size()));
    }

    private List<QuizQuestion> pickQuestions(List<QuizQuestion> questions, int limit) {
        if (questions.size() <= limit) {
            return new ArrayList<>(questions);
        }
        List<QuizQuestion> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, limit);
    }

    private QuizItemDto toQuizItemDto(QuizQuestion question) {
        List<String> choices = optionRepository.findByQuestionIdOrderByLabel(question.getId())
                .stream()
                .map(QuizOption::getText)
                .collect(Collectors.toList());

        return QuizItemDto.builder()
                .itemId(String.valueOf(question.getId()))
                .type("concept")
                .question(question.getStem())
                .choices(choices)
                .references(Collections.emptyList())
                .build();
    }

    /**
     * 특정 문제의 정답 인덱스 조회
     */
    private Integer getCorrectIndex(Long questionId) {
        List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(questionId);
        for (int i = 0; i < options.size(); i++) {
            if (Boolean.TRUE.equals(options.get(i).getIsCorrect())) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 특정 문제의 해설 조회
     */
    private String getExplanation(Long questionId) {
        return questionRepository.findById(questionId)
                .map(QuizQuestion::getCorrectText)
                .orElse(null);
    }

    /**
     * 인덱스로 선택지 찾기
     */
    private QuizOption getOptionByIndex(Long questionId, Integer index) {
        List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(questionId);
        if (index != null && index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return null;
    }

    /**
     * 선택지 ID로 인덱스 찾기
     */
    private Integer getOptionIndex(Long questionId, Long optionId) {
        if (optionId == null) {
            return -1;
        }

        List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(questionId);
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getId().equals(optionId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 특정 문제의 모든 선택지 텍스트 조회
     */
    private List<String> getChoices(Long questionId) {
        return optionRepository.findByQuestionIdOrderByLabel(questionId)
                .stream()
                .map(QuizOption::getText)
                .collect(Collectors.toList());
    }
}