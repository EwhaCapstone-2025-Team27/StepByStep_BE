// src/main/java/com/dragon/stepbystep/controller/AIController.java
package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.service.PointRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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
}