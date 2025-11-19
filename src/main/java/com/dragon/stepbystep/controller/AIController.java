// src/main/java/com/dragon/stepbystep/controller/AIController.java
package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.service.PointRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")   // 최종 경로: POST /api/chat/stream
public class AIController {

    private final AIClient ai;
    private final PointRewardService pointRewardService;

    private String userId(Authentication auth) {
        return (auth != null && auth.getName() != null) ? auth.getName() : "0";
    }

    private Long parseUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * 스트리밍 챗봇 엔드포인트
     * FE → BE: POST /api/chat/stream
     * BE → AI: AIClient.chatStream(...) → /api/chat/stream (AI 서버)
     */
    @PostMapping(
            value = "/chat/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> chatStream(@RequestBody String body, Authentication auth) {

        Long uid = parseUserId(auth);
        if (uid != null) {
            // 질문 1번에 대한 포인트 적립
            pointRewardService.rewardForChatQuestion(uid);
        }

        String userId = userId(auth);
        log.debug("chat stream start, userId={}", userId);

        // SSE 그대로 프록시
        return ai.chatStream(body, userId);
    }
}