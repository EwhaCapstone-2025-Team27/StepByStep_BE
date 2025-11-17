package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.service.PointRewardService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIController {

    private final AIClient ai;
    private final ObjectMapper om;
    private final PointRewardService pointRewardService;

    private String userId(Authentication auth) {
        return (auth != null && auth.getName() != null) ? auth.getName() : "0";
    }

    private Long parseUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @PostMapping({"/chat", "/v1/chat"})
    public ResponseEntity<JsonNode> chat(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.chat(body.toString(), userId(auth));
        JsonNode aiJson = om.readTree(raw);
        Long userId = parseUserId(auth);
        if (userId != null) {
            pointRewardService.rewardForChatQuestion(userId);
        }
        return ResponseEntity.ok(aiJson);
    }

    @PostMapping(value = {"/chat/stream", "/v1/chat/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody JsonNode body, Authentication auth) {
        Long userId = parseUserId(auth);
        if (userId != null) {
            pointRewardService.rewardForChatQuestion(userId);
        }
        return ai.chatStream(body.toString(), userId(auth));
    }

    @GetMapping({"/search", "/v1/search"})
    public ResponseEntity<JsonNode> search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "k", required = false) Integer k,
            Authentication auth) throws Exception {
        String raw = ai.search(query, k, userId(auth));
        JsonNode aiJson = om.readTree(raw);
        return ResponseEntity.ok(aiJson);
    }
}