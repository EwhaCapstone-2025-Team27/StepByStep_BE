package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardPostSummaryDto {
    private final Long postId;
    private final String nickname;
    private final LocalDateTime createdAt;
    private final String content;
    private final int commentsNum;
    private final int likesNum;

    public static BoardPostSummaryDto from(Board board) {
        return BoardPostSummaryDto.builder()
                .postId(board.getId())
                .nickname(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt() != null ? board.getUpdatedAt() : board.getCreatedAt())
                .content(board.getContent())
                .commentsNum(board.getCommentsCount())
                .likesNum(board.getLikesCount())
                .build();
    }
}