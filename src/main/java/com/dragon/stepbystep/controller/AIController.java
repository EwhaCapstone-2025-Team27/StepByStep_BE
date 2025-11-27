// src/main/java/com/dragon/stepbystep/controller/AIController.java
package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.dto.ChatMessageRequest;
import com.dragon.stepbystep.dto.ChatResponse;
import com.dragon.stepbystep.service.ChatService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")   // 최종 경로: POST /api/chat
@CrossOrigin(origins = "*")
public class AIController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatMessageRequest request, Authentication auth) {
        return chatService.chat(request, auth);
    }
}