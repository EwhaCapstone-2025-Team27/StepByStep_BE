package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.*;
import com.dragon.stepbystep.dto.quiz.*;
import com.dragon.stepbystep.repository.*;
import com.dragon.stepbystep.ai.AIClient; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final int POINT_ON_CORRECT = 20;

    private final QuizScenarioRepository scenarioRepo;
    private final QuizQuestionRepository questionRepo;
    private final QuizOptionRepository optionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizResponseRepository responseRepo;

    private final AIClient aiClient; // FastAPI(or 내부)로 퀴즈 생성 요청

    /* -------------------------------------
     * 1) 키워드
     * ------------------------------------- */
    @Transactional(readOnly = true)
    public QuizKeywordListResponseDto getKeywords(String q, int limit, Long userId) {
        // AI 쪽에서 노출 가능한 키워드 목록 가져오기
        List<QuizKeywordDto> items = aiClient.fetchQuizKeywords(q, limit);
        return new QuizKeywordListResponseDto(items);
    }

    /* -------------------------------------
     * 2) 퀴즈 세트 생성
     * ------------------------------------- */
    @Transactional
    public QuizCreateResponseDto createQuiz(String mode, String keyword, int n, Integer seed, Long userId) {
        if (!Objects.equals(mode, "by_keyword") && !Objects.equals(mode, "random")) {
            throw new IllegalArgumentException("mode must be by_keyword|random");
        }
        if (Objects.equals(mode, "by_keyword") && (keyword == null || keyword.isBlank())) {
            throw new IllegalArgumentException("keyword required");
        }

        // 2-1. AI에게 아이템 생성 요청(정답은 서버-세션/DB에만 저장)
        AIClient.QuizBundle bundle = aiClient.generateQuiz(mode, keyword, n, seed);

        // 2-2. 시나리오/질문/보기 저장(정답은 isCorrect로 옵션에 표시되거나, 별도 맵 보관)
        QuizScenario scenario = new QuizScenario();
        scenario.setTitle(keyword != null ? keyword : "랜덤");
        scenario = scenarioRepo.save(scenario);

        Map<String, Integer> answerIndexByItemId = new HashMap<>();
        List<QuizItemDto> itemDtos = new ArrayList<>();

        for (AIClient.QuizItem ai : bundle.items()) {
            QuizQuestion q = new QuizQuestion();
            q.setScenarioId(scenario.getId());
            q.setStem(ai.question());
            q.setCorrectText(ai.rationale()); // 없으면 null
            q = questionRepo.save(q);

            List<String> choices = ai.choices();
            List<QuizOption> savedOptions = new ArrayList<>();
            for (int i = 0; i < choices.size(); i++) {
                QuizOption op = new QuizOption();
                op.setQuestionId(q.getId());
                op.setText(choices.get(i));
                op.setIsCorrect(i == ai.answerIndex()); // 정답 저장
                op.setLabel(String.valueOf((char)('A' + i)));
                savedOptions.add(op);
            }
            optionRepo.saveAll(savedOptions);

            String itemId = "it_" + String.format("%02d", itemDtos.size() + 1);
            answerIndexByItemId.put(itemId, ai.answerIndex());

            itemDtos.add(new QuizItemDto(
                    itemId,
                    ai.type(),
                    ai.question(),
                    choices,
                    ai.references()
            ));
        }

        // 2-3. Attempt 생성(정답 맵을 attempt의 score_total/answers 등에 바로 쓰지 않고,
        //     제출 시 비교. 필요하면 Redis/별도 테이블로 이전 가능)
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setScenarioId(scenario.getId());
        attempt.setStatus(QuizAttempt.Status.IN_PROGRESS.name());
        attempt.setStartedAt(OffsetDateTime.now());
        attempt.setScoreMax(itemDtos.size() * POINT_ON_CORRECT);
        attempt.setScoreTotal(0);
        attempt = attemptRepo.save(attempt);

        // 정답 맵을 세션화할 저장소가 별도로 없다면, 간단히 서버 메모리 캐시/Redis를 추천.
        // 여기서는 AIClient가 돌려준 quizId를 그대로 사용하고, service 내부 캐시에 보관.
        aiClient.cacheAnswerMap(attempt.getId(), answerIndexByItemId);

        return new QuizCreateResponseDto(
                "qz_" + Long.toString(attempt.getId(), 36),
                mode,
                keyword,
                itemDtos.size(),
                itemDtos
        );
    }

    /* -------------------------------------
     * 3) 제출
     * ------------------------------------- */
    @Transactional
    public QuizSubmitAnswerResponseDto submitAnswer(QuizSubmitAnswerRequestDto req, Long userId) {
        Long attemptId = parseAttemptId(req.getQuizId());
        QuizAttempt attempt = attemptRepo.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        if (!Objects.equals(attempt.getStatus(), QuizAttempt.Status.IN_PROGRESS.name())) {
            throw new IllegalStateException("Quiz session not in progress");
        }

        Map<String, Integer> answerMap = aiClient.getCachedAnswerMap(attemptId);
        if (answerMap == null) {
            throw new IllegalStateException("SESSION_EXPIRED");
        }
        Integer correctIdx = answerMap.get(req.getItemId());
        if (correctIdx == null) {
            throw new NoSuchElementException("Item not found");
        }

        // 중복 제출 방지: 이미 해당 itemId로 응답이 있으면 409 처리
        boolean already = responseRepo.existsById(makeResponseId(attemptId, req.getItemId()));
        if (already) {
            // 멱등 응답
            return new QuizSubmitAnswerResponseDto(false, correctIdx, null, 0, null, req.getQuizId());
        }

        boolean isCorrect = (req.getChoiceIndex() == correctIdx);

        // 저장
        QuizResponse r = new QuizResponse();
        r.setId(makeResponseId(attemptId, req.getItemId())); // 복합키를 단일키로 구성
        r.setAttemptId(attemptId);
        r.setQuestionId(findQuestionIdByItemIndex(attemptId, req.getItemId())); // 필요 시 매핑 로직 구현
        r.setCreatedAt(OffsetDateTime.now());
        r.setIsCorrect(isCorrect);
        r.setScore(isCorrect ? POINT_ON_CORRECT : 0);
        r.setOptionId(null); // 선택 보기가 DB에 있다면 매핑해서 채워도 됨
        responseRepo.save(r);

        if (isCorrect) {
            attempt.setScoreTotal(Optional.ofNullable(attempt.getScoreTotal()).orElse(0) + POINT_ON_CORRECT);
            attemptRepo.save(attempt);

            // 포인트 적립 히스토리(네 프로젝트에 PointHistoryService/Repo가 있으면 여기서 호출)
            // pointHistoryService.record(userId, "QUIZ_CORRECT", attemptId + ":" + req.getItemId(), POINT_ON_CORRECT);
        }

        return new QuizSubmitAnswerResponseDto(
                isCorrect,
                correctIdx,
                null,
                isCorrect ? POINT_ON_CORRECT : 0,
                null,
                req.getQuizId()
        );
    }

    /* -------------------------------------
     * 4) 결과
     * ------------------------------------- */
    @Transactional(readOnly = true)
    public QuizResultResponseDto getResult(String resultId, Long userId) {
        Long attemptId = parseAttemptId(resultId);
        QuizAttempt attempt = attemptRepo.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new NoSuchElementException("Result not found"));

        // 응답 모아오기
        List<QuizResponse> responses = responseRepo.findAll().stream()
                .filter(r -> Objects.equals(r.getAttemptId(), attemptId))
                .collect(Collectors.toList());

        // 정답맵(세션 만료 가능성 있음) — 없더라도 저장된 정답(QuizOption.isCorrect)로 재계산 가능
        Map<String, Integer> answerMap = Optional.ofNullable(aiClient.getCachedAnswerMap(attemptId))
                .orElseGet(HashMap::new);

        int correctCnt = (int) responses.stream().filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count();
        int earnedTotal = responses.stream().mapToInt(r -> Optional.ofNullable(r.getScore()).orElse(0)).sum();

        List<QuizResultItemDto> items = new ArrayList<>();
        for (Map.Entry<String, Integer> e : answerMap.entrySet()) {
            String itemId = e.getKey();
            int correctIdx = e.getValue();
            Optional<QuizResponse> your = responses.stream()
                    .filter(r -> itemId.equals(extractItemId(r.getId())))
                    .findFirst();

            boolean ok = your.map(r -> Boolean.TRUE.equals(r.getIsCorrect())).orElse(false);
            int earned = your.map(r -> Optional.ofNullable(r.getScore()).orElse(0)).orElse(0);
            Integer yourChoice = your.map(r -> ok ? correctIdx : null).orElse(null); // 저장 안했으면 null
            if (yourChoice == null && your.isPresent()) {
                // 선택 인덱스를 저장 안했으면 -1로
                yourChoice = -1;
            }

            items.add(new QuizResultItemDto(
                    itemId, yourChoice, correctIdx, ok, earned,
                    null, null, null
            ));
        }

        return new QuizResultResponseDto(
                resultId,
                answerMap.size(),
                correctCnt,
                earnedTotal,
                items
        );
    }

    /* --------- 유틸들 --------- */

    private Long parseAttemptId(String quizId) {
        // "qz_" prefix 제거하고 36진수 -> 10진수
        String token = quizId.startsWith("qz_") ? quizId.substring(3) : quizId;
        return Long.parseLong(token, 36);
    }

    private String makeResponseId(Long attemptId, String itemId) {
        return attemptId + ":" + itemId;
    }

    private String extractItemId(String responseId) {
        int i = responseId.indexOf(':');
        return (i > 0) ? responseId.substring(i + 1) : responseId;
    }

    private Long findQuestionIdByItemIndex(Long attemptId, String itemId) {
        // 간단 버전: 필요 시 attemptId -> scenarioId -> n번째 질문 ID 매핑 캐시를 만들어 두는 것을 추천
        // 지금은 null 리턴해도 동작엔 지장 없음(Option/Question 외래키 강제 아닐 시)
        return null;
    }
}