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
                .createdAt(board.getCreatedAt())
                .content(board.getContent())
                .build();
    }
}