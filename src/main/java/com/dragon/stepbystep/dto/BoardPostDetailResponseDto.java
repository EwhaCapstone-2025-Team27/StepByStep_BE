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
    private final Long userId;
    private final boolean isMine;

    public static BoardPostDetailResponseDto from(Board board, List<BoardCommentResponseDto> comments, Long currentUserId) {
        List<BoardCommentResponseDto> safeComments = comments == null ? Collections.emptyList() : List.copyOf(comments);
        int commentsNum = safeComments.isEmpty() ? board.getCommentsCount() : safeComments.size();
        Long authorId = board.getAuthor() != null ? board.getAuthor().getId() : null;
        boolean mine = currentUserId != null && board.isAuthor(currentUserId);

        return BoardPostDetailResponseDto.builder()
                .id(board.getId())
                .nickname(resolveNickname(board))
                .createdAt(board.getCreatedAt())
                .content(board.getContent())
                .commentsNum(commentsNum)
                .likesNum(board.getLikesCount())
                .data(safeComments)
                .userId(authorId)
                .isMine(mine)
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