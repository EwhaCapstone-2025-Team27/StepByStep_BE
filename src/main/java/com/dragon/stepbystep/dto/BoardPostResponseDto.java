package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardPostResponseDto {
    private final Long id;
    private final String nickname;
    private final LocalDateTime createdAt;
    private final String content;
    private final Long userId;
    private final boolean isMine;

    public static BoardPostResponseDto from(Board board) {
        return from(board, null);
    }

    public static BoardPostResponseDto from(Board board, Long currentUserId) {
        Long authorId = board.getAuthor() != null ? board.getAuthor().getId() : null;
        boolean mine = currentUserId != null && board.isAuthor(currentUserId);

        return BoardPostResponseDto.builder()
                .id(board.getId())
                .nickname(resolveNickname(board))
                .createdAt(board.getCreatedAt())
                .content(board.getContent())
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
}