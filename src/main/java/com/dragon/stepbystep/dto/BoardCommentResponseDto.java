package com.dragon.stepbystep.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardCommentResponseDto {
    private final Long commentId;
    private final String nickname;
    private final LocalDateTime createdAt;
    private final String comments;
}