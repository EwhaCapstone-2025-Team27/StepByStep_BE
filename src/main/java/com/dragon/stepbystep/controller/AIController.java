package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.http.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIController {

    private final AIClient ai;
    private final ObjectMapper om;

    private String userId(Authentication auth) {
        // JwtAuthenticationFilter가 principal.getName() = userId 로 설정되도록 수정되어 있어야 함
        return (auth != null && auth.getName() != null) ? auth.getName() : "0";
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<JsonNode>> chat(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.chat(body.toString(), userId(auth));
        JsonNode aiJson = om.readTree(raw);
        return ResponseEntity.ok(ApiResponse.success(aiJson)); // 팀 규격 유지
    }
}

@PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStream(@RequestBody JsonNode body, Authentication auth) {
    return ai.chatStream(body.toString(), userId(auth));
}