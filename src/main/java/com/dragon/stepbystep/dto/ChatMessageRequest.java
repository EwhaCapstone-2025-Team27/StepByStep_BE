package com.dragon.stepbystep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotBlank
    private String message;

    private String userId;

    private Integer top_k;

    private Boolean enable_bm25;

    private Boolean enable_rrf;
}