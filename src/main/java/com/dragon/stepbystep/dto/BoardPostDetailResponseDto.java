package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class BoardPostDetailResponseDto {
    private final Long id;
    private final String nickname;
    private final LocalDateTime createdAt;
    private final String content;
    private final int commentsNum;
    private final int likesNum;
    private final List<BoardCommentResponseDto> data;

    public static BoardPostDetailResponseDto from(Board board, List<BoardCommentResponseDto> comments) {
        List<BoardCommentResponseDto> safeComments = comments == null ? Collections.emptyList() : List.copyOf(comments);
        LocalDateTime displayTime = board.getUpdatedAt() != null ? board.getUpdatedAt() : board.getCreatedAt();
        int commentsNum = safeComments.isEmpty() ? board.getCommentsCount() : safeComments.size();

        return BoardPostDetailResponseDto.builder()
                .id(board.getId())
                .nickname(resolveNickname(board))
                .createdAt(displayTime)
                .content(board.getContent())
                .commentsNum(commentsNum)
                .likesNum(board.getLikesCount())
                .data(safeComments)
                .build();
    }

    private static String resolveNickname(Board board) {
        String storedNickname = board.getAuthorNickname();
        if (storedNickname != null && !storedNickname.isEmpty()) {
            return storedNickname;
        }
        return board.getAuthor() != null ? board.getAuthor().getNickname() : null;
    }
}