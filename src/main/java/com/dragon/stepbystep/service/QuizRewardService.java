package com.dragon.stepbystep.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizRewardService {

    private static final List<String> CORRECT_COUNT_KEYS = List.of(
            "correctCount",
            "correct_count",
            "correctAnswerCount",
            "correctAnswersCount",
            "correct_answers_count",
            "numCorrect",
            "num_correct"
    );
    private static final List<String> ANSWER_ARRAY_KEYS = List.of(
            "answers",
            "answerDetails",
            "answer_details",
            "responses",
            "questions",
            "items"
    );

    private final PointRewardService pointRewardService;

    @Transactional
    public void rewardCorrectAnswers(Long userId, JsonNode quizResultNode) {
        if (userId == null || quizResultNode == null || quizResultNode.isNull()) {
            return;
        }

        Integer correctCount = findCorrectCount(quizResultNode);
        if (correctCount == null || correctCount <= 0) {
            return;
        }
        pointRewardService.rewardForQuizCorrectAnswers(userId, correctCount);
    }

    private Integer findCorrectCount(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isObject()) {
            for (String key : CORRECT_COUNT_KEYS) {
                JsonNode value = node.get(key);
                if (value != null && value.isNumber()) {
                    return value.intValue();
                }
            }

            Integer countFromArrays = countFromAnswerArrays(node);
            if (countFromArrays != null) {
                return countFromArrays;
            }

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                Integer nested = findCorrectCount(entry.getValue());
                if (nested != null) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            Integer directCount = countFromAnswerArray(node);
            if (directCount != null) {
                return directCount;
            }

            for (JsonNode child : node) {
                Integer nested = findCorrectCount(child);
                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }

    private Integer countFromAnswerArrays(JsonNode node) {
        for (String key : ANSWER_ARRAY_KEYS) {
            Integer count = countFromAnswerArray(node.get(key));
            if (count != null) {
                return count;
            }
        }
        return null;
    }

    private Integer countFromAnswerArray(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }

        boolean hasCorrectFlag = false;
        int correctCount = 0;

        for (JsonNode item : arrayNode) {
            if (item == null || item.isNull()) {
                continue;
            }

            if (item.has("isCorrect")) {
                hasCorrectFlag = true;
                if (item.path("isCorrect").asBoolean(false)) {
                    correctCount++;
                }
            } else if (item.has("is_correct")) {
                hasCorrectFlag = true;
                if (item.path("is_correct").asBoolean(false)) {
                    correctCount++;
                }
            } else if (item.has("correct")) {
                JsonNode correctNode = item.get("correct");
                if (correctNode.isBoolean()) {
                    hasCorrectFlag = true;
                    if (correctNode.asBoolean(false)) {
                        correctCount++;
                    }
                } else if (correctNode.isTextual()) {
                    hasCorrectFlag = true;
                    if (Boolean.parseBoolean(correctNode.asText())) {
                        correctCount++;
                    }
                }
            } else if (item.has("result")) {
                JsonNode resultNode = item.get("result");
                if (resultNode.isBoolean()) {
                    hasCorrectFlag = true;
                    if (resultNode.asBoolean(false)) {
                        correctCount++;
                    }
                }
            }
        }

        return hasCorrectFlag ? correctCount : null;
    }
}