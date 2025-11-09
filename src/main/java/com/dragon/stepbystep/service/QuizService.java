// QuizService.java (개선 버전)
package com.dragon.stepbystep.service;

import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.domain.*;
import com.dragon.stepbystep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizScenarioRepository scenarioRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizResponseRepository responseRepository;
    private final RestTemplate restTemplate;

    // application.properties에서 설정값 주입
    @Value("${quiz.ai.server.url}")
    private String aiServerUrl;

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
     * 1. 퀴즈 생성 (AI 서버 호출 → DB 저장)
     */
    @Transactional
    public QuizGetResponseDto generateQuiz(QuizGenerateRequestDto request, Long userId) {
        //  유효성 검증
        validateQuizRequest(request);

        log.info("퀴즈 생성 요청: userId={}, mode={}, keyword={}, count={}",
                userId, request.getMode(), request.getKeyword(), request.getCount());

        // AI 서버 호출 (설정값 사용)
        String url = String.format("%s?mode=%s&keyword=%s&n=%d&user_id=%d",
                aiServerUrl,
                request.getMode(),
                request.getKeyword() != null ? request.getKeyword() : "",
                request.getCount() != null ? request.getCount() : defaultCount,
                userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<QuizGetResponseDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, QuizGetResponseDto.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error("AI 서버 퀴즈 생성 실패: status={}", response.getStatusCode());
            throw new RuntimeException("AI 서버 퀴즈 생성 실패");
        }

        QuizGetResponseDto aiResponse = response.getBody();

        log.info("AI 서버 응답 성공: quizId={}, total={}",
                aiResponse.getQuizId(), aiResponse.getTotal());

        return aiResponse;
    }

    /**
     * 2. 답안 제출 및 채점
     */
    @Transactional
    public SubmitAnswerResponseDto submitAnswer(SubmitAnswerRequestDto request) {
        Long attemptId = Long.parseLong(request.getQuizId());
        Long questionId = Long.parseLong(request.getItemId());
        Integer choiceIndex = request.getChoiceIndex();

        log.info("답안 제출: attemptId={}, questionId={}, choiceIndex={}",
                attemptId, questionId, choiceIndex);

        // 중복 제출 체크 (멱등성)
        Optional<QuizResponse> existingResponse = responseRepository
                .findByAttemptIdAndQuestionId(attemptId, questionId);

        if (existingResponse.isPresent()) {
            log.warn("중복 제출 감지: attemptId={}, questionId={}", attemptId, questionId);
            QuizResponse existing = existingResponse.get();
            Integer correctIndex = getCorrectIndex(questionId);
            String explanation = getExplanation(questionId);

            return SubmitAnswerResponseDto.builder()
                    .correct(existing.getIsCorrect())
                    .correctIndex(correctIndex)
                    .explanation(explanation)
                    .earnedPoints(0)  // 중복 제출은 포인트 없음
                    .balance(0)
                    .resultId(request.getQuizId())
                    .build();
        }

        // 정답 확인
        Integer correctIndex = getCorrectIndex(questionId);
        boolean isCorrect = choiceIndex.equals(correctIndex);

        // 선택한 옵션 찾기
        QuizOption selectedOption = getOptionByIndex(questionId, choiceIndex);

        // 응답 저장
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("QuizAttempt not found: " + attemptId));
        QuizQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("QuizQuestion not found: " + questionId));

        QuizResponse response = QuizResponse.builder()
                .attempt(attempt)
                .question(question)
                .option(selectedOption)
                .isCorrect(isCorrect)
                .score(isCorrect ? 1 : 0)
                .createdAt(LocalDateTime.now())
                .build();

        responseRepository.save(response);

        // 점수 업데이트
        if (isCorrect) {
            attempt.setScoreTotal((attempt.getScoreTotal() != null ? attempt.getScoreTotal() : 0) + 1);
            attemptRepository.save(attempt);
            log.info("정답! attemptId={}, questionId={}, 누적점수={}",
                    attemptId, questionId, attempt.getScoreTotal());
        } else {
            log.info("오답: attemptId={}, questionId={}, 선택={}, 정답={}",
                    attemptId, questionId, choiceIndex, correctIndex);
        }

        // 해설 조회
        String explanation = getExplanation(questionId);

        //  설정값에서 포인트 가져오기
        return SubmitAnswerResponseDto.builder()
                .correct(isCorrect)
                .correctIndex(correctIndex)
                .explanation(explanation != null ? explanation : "해설이 없습니다.")
                .earnedPoints(isCorrect ? pointsCorrect : pointsIncorrect)
                .balance(0)
                .resultId(request.getQuizId())
                .build();
    }

    /**
     * 3. 결과 조회
     */
    @Transactional(readOnly = true)
    public QuizResultResponseDto getResult(Long attemptId) {
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

        return QuizResultResponseDto.builder()
                .resultId(String.valueOf(attemptId))
                .total(items.size())
                .correctCount(correctCount)
                .earnedPointsTotal(correctCount * pointsCorrect)  //  설정값 사용
                .items(items)
                .build();
    }

    // ===== Private Helper Methods =====

    /**
     * 퀴즈 요청 유효성 검증
     */
    private void validateQuizRequest(QuizGenerateRequestDto request) {
        if (request.getMode() == null ||
                (!request.getMode().equals("by_keyword") && !request.getMode().equals("random"))) {
            throw new IllegalArgumentException("mode는 'by_keyword' 또는 'random'이어야 합니다.");
        }

        if ("by_keyword".equals(request.getMode()) &&
                (request.getKeyword() == null || request.getKeyword().trim().isEmpty())) {
            throw new IllegalArgumentException("by_keyword 모드에서는 keyword가 필수입니다.");
        }

        if (request.getCount() != null) {
            if (request.getCount() < minCount || request.getCount() > maxCount) {
                throw new IllegalArgumentException(
                        String.format("문제 수는 %d~%d 사이여야 합니다.", minCount, maxCount));
            }
        }
    }

    private Integer getCorrectIndex(Long questionId) {
        List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(questionId);
        for (int i = 0; i < options.size(); i++) {
            if (Boolean.TRUE.equals(options.get(i).getIsCorrect())) {
                return i;
            }
        }
        return 0;
    }

    private String getExplanation(Long questionId) {
        return questionRepository.findById(questionId)
                .map(QuizQuestion::getCorrectText)
                .orElse(null);
    }

    private QuizOption getOptionByIndex(Long questionId, Integer index) {
        List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(questionId);
        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return null;
    }

    private Integer getOptionIndex(Long questionId, Long optionId) {
        if (optionId == null) return -1;

        List<QuizOption> options = optionRepository.findByQuestionIdOrderByLabel(questionId);
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getId().equals(optionId)) {
                return i;
            }
        }
        return -1;
    }

    private List<String> getChoices(Long questionId) {
        return optionRepository.findByQuestionIdOrderByLabel(questionId)
                .stream()
                .map(QuizOption::getText)
                .collect(Collectors.toList());
    }
}