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

    public static BoardPostResponseDto from(Board board) {
        return BoardPostResponseDto.builder()
                .id(board.getId())
                .nickname(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt() != null ? board.getCreatedAt() : board.getUpdatedAt())
                .content(board.getContent())
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