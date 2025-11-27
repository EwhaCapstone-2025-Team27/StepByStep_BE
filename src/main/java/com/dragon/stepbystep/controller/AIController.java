// src/main/java/com/dragon/stepbystep/controller/AIController.java
package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.service.PointRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")   // 최종 경로: POST /api/chat
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

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody String body, Authentication auth) {
        Flux<String> stream = ai.chatStream(body, userId(auth));

        Long uid = parseUserId(auth);
        if (uid == null) {
            return stream;
        }

        return stream.doOnComplete(() -> pointRewardService.rewardForChatQuestion(uid));
    }
}