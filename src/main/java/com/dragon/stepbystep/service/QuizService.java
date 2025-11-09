package com.dragon.stepbystep.service;

import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.domain.*;
import com.dragon.stepbystep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
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

    // ===== Dependencies =====
    private final RestTemplate restTemplate;

    // ===== Configuration Properties =====
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
     *
     * @param keyword 퀴즈 키워드
     * @param count 생성할 문제 수
     * @return AI 서버에서 반환한 퀴즈 DTO
     */
    @Transactional
    public QuizGetResponseDto generateQuiz(String keyword, Integer count) {
        try {
            // count 기본값 설정
            if (count == null) {
                count = defaultCount;
            }

            // count 범위 검증
            if (count < minCount || count > maxCount) {
                log.warn("요청된 count={} 범위 초과 (min={}, max={})", count, minCount, maxCount);
                count = Math.min(Math.max(count, minCount), maxCount);
            }

            log.info(" AI 서버 호출 시작: keyword={}, count={}, url={}", keyword, count, aiServerUrl);

            // AI 서버 요청 데이터
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("keyword", keyword);
            requestBody.put("count", count);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // AI 서버 호출
            ResponseEntity<QuizGetResponseDto> response = restTemplate.postForEntity(
                    aiServerUrl,
                    request,
                    QuizGetResponseDto.class
            );

            // 응답 검증
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error(" AI 서버 퀴즈 생성 실패: status={}", response.getStatusCode());
                throw new RuntimeException("AI 서버 퀴즈 생성 실패: " + response.getStatusCode());
            }

            QuizGetResponseDto aiResponse = response.getBody();

            log.info(" AI 서버 응답 성공: quizId={}, total={}",
                    aiResponse.getQuizId(), aiResponse.getTotal());

            return aiResponse;

        } catch (Exception e) {
            log.error(" AI 서버 호출 중 예외 발생", e);
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