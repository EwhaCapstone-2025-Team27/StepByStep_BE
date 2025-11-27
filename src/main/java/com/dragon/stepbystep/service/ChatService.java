package com.dragon.stepbystep.service;

import com.dragon.stepbystep.ai.RagClient;
import com.dragon.stepbystep.dto.ChatMessageRequest;
import com.dragon.stepbystep.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RagClient ragClient;
    private final PointRewardService pointRewardService;

    public ChatResponse chat(ChatMessageRequest request, Authentication authentication) {
        String headerUserId = resolveHeaderUserId(authentication, request.getUserId());
        ChatResponse response = ragClient.chat(request, headerUserId);

        Long rewardUserId = resolveNumericUserId(authentication, request.getUserId());
        if (rewardUserId != null) {
            pointRewardService.rewardForChatQuestion(rewardUserId);
        }

        return response;
    }

    private String resolveHeaderUserId(Authentication auth, String fallback) {
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return (fallback != null && !fallback.isBlank()) ? fallback : "0";
    }

    private Long resolveNumericUserId(Authentication auth, String fallback) {
        String source = auth != null ? auth.getName() : fallback;
        if (source == null) return null;
        try {
            return Long.valueOf(source);
        } catch (NumberFormatException ignored) {
            log.debug("숫자로 변환할 수 없는 사용자 ID: {}", source);
            return null;
        }
    }
}