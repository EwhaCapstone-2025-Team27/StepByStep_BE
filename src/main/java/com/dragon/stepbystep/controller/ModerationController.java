package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.ai.AIClient;
import com.dragon.stepbystep.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/moderation")
public class ModerationController {

    private final AIClient ai;
    private final ObjectMapper om;

    private String userId(Authentication auth) {
        return (auth != null && auth.getName() != null) ? auth.getName() : "0";
    }

    // ---------- 1) 단건 텍스트 검사 ----------
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<JsonNode>> check(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.moderation("/check", body.toString(), userId(auth));
        return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
    }

    // ---------- 2) 게시글 검사 ----------
    @PostMapping("/guard-post")
    public ResponseEntity<ApiResponse<JsonNode>> guardPost(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.moderation("/guard-post", body.toString(), userId(auth));
        return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
    }

    // ---------- 3) 댓글 검사 ----------
    @PostMapping("/guard-comment")
    public ResponseEntity<ApiResponse<JsonNode>> guardComment(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.moderation("/guard-comment", body.toString(), userId(auth));
        return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
    }

    // ---------- 4) (선택) 내부 LLM 입출력 필터 ----------
    @PostMapping("/guard-input")
    public ResponseEntity<ApiResponse<JsonNode>> guardInput(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.moderation("/guard-input", body.toString(), userId(auth));
        return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
    }

    @PostMapping("/guard-output")
    public ResponseEntity<ApiResponse<JsonNode>> guardOutput(@RequestBody JsonNode body, Authentication auth) throws Exception {
        String raw = ai.moderation("/guard-output", body.toString(), userId(auth));
        return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
    }
}
@PostMapping("/check-batch")
public ResponseEntity<ApiResponse<JsonNode>> checkBatch(@RequestBody JsonNode body, Authentication auth) throws Exception {
    String raw = ai.moderationCheckBatch(body.toString(), userId(auth));
    return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
}

@PostMapping("/filter-snippets")
public ResponseEntity<ApiResponse<JsonNode>> filterSnippets(@RequestBody JsonNode body, Authentication auth) throws Exception {
    String raw = ai.moderationFilterSnippets(body.toString(), userId(auth));
    return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
}

@PostMapping("/guard-batch")
public ResponseEntity<ApiResponse<JsonNode>> guardBatch(@RequestBody JsonNode body, Authentication auth) throws Exception {
    String raw = ai.moderationGuardBatch(body.toString(), userId(auth));
    return ResponseEntity.ok(ApiResponse.success(om.readTree(raw)));
}