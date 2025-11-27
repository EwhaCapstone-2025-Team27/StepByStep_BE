package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.QuizAttempt;
import com.dragon.stepbystep.domain.QuizOption;
import com.dragon.stepbystep.domain.QuizQuestion;
import com.dragon.stepbystep.domain.QuizResponse;
import com.dragon.stepbystep.domain.QuizScenario;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private static final int DEFAULT_QUESTION_COUNT = 2;

    private final QuizScenarioRepository scenarioRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizResponseRepository responseRepository;
    private final PointRewardService pointRewardService;

    @Transactional(readOnly = true)
    public List<QuizScenarioDto> getKeywords() {
        return scenarioRepository.findAll().stream()
                .sorted(Comparator.comparingLong(QuizScenario::getId))
                .map(s -> QuizScenarioDto.builder()
                        .id(s.getId())
                        .title(s.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    public QuizAttemptCreateResponseDto createAttempt(QuizAttemptCreateRequestDto request, Long userId) {
        QuizAttempt.QuizMode mode = Optional.ofNullable(request.getMode()).orElse(QuizAttempt.QuizMode.KEYWORD);
        int questionCount = DEFAULT_QUESTION_COUNT; // 한 세트는 2문제 고정

        QuizScenario scenario = selectScenario(mode, request.getScenarioId(), questionCount);
        List<QuizQuestion> selectedQuestions = selectQuestions(scenario.getId(), questionCount);

        QuizAttempt attempt = QuizAttempt.builder()
                .userId(userId == null ? 0L : userId)
                .scenario(scenario)
                .scoreMax(questionCount)
                .mode(mode)
                .build();
        attemptRepository.save(attempt);

        List<QuizQuestionResponseDto> questionDtos = new ArrayList<>();
        int index = 1;
        for (QuizQuestion question : selectedQuestions) {
            List<QuizOptionResponseDto> options = optionRepository.findByQuestionIdOrderByLabel(question.getId()).stream()
                    .map(opt -> QuizOptionResponseDto.builder()
                            .optionId(opt.getId())
                            .label(opt.getLabel())
                            .text(opt.getText())
                            .build())
                    .collect(Collectors.toList());

            questionDtos.add(QuizQuestionResponseDto.builder()
                    .index(index++)
                    .questionId(question.getId())
                    .stem(question.getStem())
                    .options(options)
                    .build());
        }

        return QuizAttemptCreateResponseDto.builder()
                .attemptId(attempt.getId())
                .scenario(QuizScenarioDto.builder()
                        .id(scenario.getId())
                        .title(scenario.getTitle())
                        .build())
                .questionCount(questionCount)
                .questions(questionDtos)
                .build();
    }

    public QuizAnswerSubmitResponseDto submitResponse(Long attemptId, QuizAnswerSubmitRequestDto request) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt를 찾을 수 없습니다."));

        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 완료된 시도입니다.");
        }

        Long questionId = request.getQuestionId();
        Long optionId = request.getOptionId();
        if (questionId == null || optionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionId와 optionId는 필수입니다.");
        }

        QuizQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문항을 찾을 수 없습니다."));

        if (!question.getScenario().getId().equals(attempt.getScenario().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시나리오에 속하지 않은 문항입니다.");
        }

        responseRepository.findByAttemptIdAndQuestionId(attemptId, questionId).ifPresent(r -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 응답한 문항입니다.");
        });

        QuizOption selectedOption = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "선택한 보기를 찾을 수 없습니다."));

        if (!selectedOption.getQuestion().getId().equals(questionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 보기가 문항에 속하지 않습니다.");
        }

        QuizOption correctOption = findCorrectOption(questionId);
        boolean correct = correctOption.getId().equals(selectedOption.getId());
        int scoreDelta = correct ? 1 : 0;
        int totalScore = (attempt.getScoreTotal() == null ? 0 : attempt.getScoreTotal()) + scoreDelta;

        QuizResponse response = QuizResponse.builder()
                .attempt(attempt)
                .question(question)
                .option(selectedOption)
                .isCorrect(correct)
                .score(scoreDelta)
                .build();
        responseRepository.save(response);

        attempt.setScoreTotal(totalScore);

        int responseCount = responseRepository.findByAttemptId(attemptId).size();
        boolean finished = responseCount >= Optional.ofNullable(attempt.getScoreMax()).orElse(DEFAULT_QUESTION_COUNT);
        if (finished) {
            attempt.setStatus(QuizAttempt.AttemptStatus.SUBMITTED);
            attempt.setSubmittedAt(LocalDateTime.now());
            pointRewardService.rewardForQuizCorrectAnswers(attempt.getUserId(), totalScore);

            Long userId = attempt.getUserId();
            if (userId != null && userId > 0 && totalScore > 0) {
                pointRewardService.rewardForQuizCorrectAnswers(userId, totalScore);
            }
        }

        return QuizAnswerSubmitResponseDto.builder()
                .correct(correct)
                .correctOptionId(correctOption.getId())
                .explanation(question.getCorrectText())
                .scoreDelta(scoreDelta)
                .totalScore(totalScore)
                .finished(finished)
                .build();
    }

    @Transactional(readOnly = true)
    public QuizAttemptResultResponseDto getAttemptResult(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt를 찾을 수 없습니다."));

        List<QuizAttemptResponseItemDto> responseDtos = new ArrayList<>();

        for (QuizResponse response : responseRepository.findByAttemptId(attemptId)) {
            QuizQuestion question = response.getQuestion();
            List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(question.getId());
            QuizOption correctOption = options.stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "정답 보기를 찾을 수 없습니다."));

            responseDtos.add(QuizAttemptResponseItemDto.builder()
                    .questionId(question.getId())
                    .stem(question.getStem())
                    .selectedOptionId(response.getOption().getId())
                    .selectedOptionLabel(response.getOption().getLabel())
                    .selectedOptionText(response.getOption().getText())
                    .isCorrect(response.getIsCorrect())
                    .correctOptionId(correctOption.getId())
                    .correctOptionLabel(correctOption.getLabel())
                    .correctOptionText(correctOption.getText())
                    .explanation(question.getCorrectText())
                    .build());
        }

        return QuizAttemptResultResponseDto.builder()
                .attemptId(attempt.getId())
                .scenario(QuizScenarioDto.builder()
                        .id(attempt.getScenario().getId())
                        .title(attempt.getScenario().getTitle())
                        .build())
                .mode(attempt.getMode())
                .scoreTotal(attempt.getScoreTotal())
                .scoreMax(attempt.getScoreMax())
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .responses(responseDtos)
                .build();
    }

    private QuizScenario selectScenario(QuizAttempt.QuizMode mode, Long scenarioId, int questionCount) {
        if (mode == QuizAttempt.QuizMode.KEYWORD) {
            if (scenarioId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scenarioId는 필수입니다.");
            }
            QuizScenario scenario = scenarioRepository.findById(scenarioId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
            validateQuestionCount(scenario.getId(), questionCount);
            return scenario;
        }

        List<QuizScenario> candidates = scenarioRepository.findAll();
        List<QuizScenario> valid = candidates.stream()
                .filter(s -> questionRepository.countByScenarioId(s.getId()) >= questionCount)
                .collect(Collectors.toList());
        if (valid.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "충분한 문제가 있는 시나리오가 없습니다.");
        }
        Collections.shuffle(valid);
        return valid.get(0);
    }

    private void validateQuestionCount(Long scenarioId, int questionCount) {
        long count = questionRepository.countByScenarioId(scenarioId);
        if (count < questionCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청한 개수의 문제를 제공할 수 없습니다.");
        }
    }

    private List<QuizQuestion> selectQuestions(Long scenarioId, int questionCount) {
        List<QuizQuestion> questions = new ArrayList<>(questionRepository.findByScenarioIdOrderByIdAsc(scenarioId));
        if (questions.size() < questionCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청한 개수의 문제를 제공할 수 없습니다.");
        }
        Collections.shuffle(questions);
        return questions.subList(0, questionCount);
    }

    private QuizOption findCorrectOption(Long questionId) {
        return optionRepository.findByQuestionIdOrderByLabel(questionId).stream()
                .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "정답 보기를 찾을 수 없습니다."));
    }
}