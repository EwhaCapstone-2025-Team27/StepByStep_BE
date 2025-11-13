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
    private final Long userId;
    private final boolean isMine;

    public static BoardPostSummaryDto from(Board board) {
        return from(board, null);
    }

    public static BoardPostSummaryDto from(Board board, Long currentUserId) {
        Long authorId = board.getAuthor() != null ? board.getAuthor().getId() : null;
        boolean mine = currentUserId != null && board.isAuthor(currentUserId);

        return BoardPostSummaryDto.builder()
                .postId(board.getId())
                .nickname(resolveNickname(board))
                .createdAt(resolveTimestamp(board))
                .content(board.getContent())
                .commentsNum(board.getCommentsCount())
                .likesNum(board.getLikesCount())
                .userId(authorId)
                .isMine(mine)
                .build();
    }

    private static String resolveNickname(Board board) {
        String storedNickname = board.getAuthorNickname();
        if (storedNickname != null && !storedNickname.isBlank()) {
            return storedNickname;
        }
        return board.getAuthor() != null ? board.getAuthor().getNickname() : null;
    }

    private static LocalDateTime resolveTimestamp(Board board) {
        LocalDateTime updatedAt = board.getUpdatedAt();
        return updatedAt != null ? updatedAt : board.getCreatedAt();
    }
}