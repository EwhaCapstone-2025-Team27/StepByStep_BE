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
    private final boolean isMine;

    public static BoardCommentResponseDto from(BoardComment comment) {
        return from(comment, null);
    }

    public static BoardCommentResponseDto from(BoardComment comment, Long currentUserId) {
        boolean mine = currentUserId != null && comment.isAuthor(currentUserId);

        return BoardCommentResponseDto.builder()
                .id(comment.getId())
                .nickname(comment.getAuthorNickname())
                .postId(comment.getBoard().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .isMine(mine)
                .build();
    }
}