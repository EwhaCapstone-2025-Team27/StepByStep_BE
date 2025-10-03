package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.BoardComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardCommentResponseDto {
    private final Long id;
    private final String nickname;
    private final Long postId;
    private final String content;
    private final LocalDateTime createdAt;

    public static BoardCommentResponseDto from(BoardComment comment) {
        return BoardCommentResponseDto.builder()
                .id(comment.getId())
                .nickname(comment.getAuthorNickname())
                .postId(comment.getBoard().getId())
                .content(comment.getContent())
                .createdAt(resolveTimestamp(comment))
                .build();
    }

    private static LocalDateTime resolveTimestamp(BoardComment comment) {
        LocalDateTime updatedAt = comment.getUpdatedAt();
        return updatedAt != null ? updatedAt : comment.getCreatedAt();
    }
}