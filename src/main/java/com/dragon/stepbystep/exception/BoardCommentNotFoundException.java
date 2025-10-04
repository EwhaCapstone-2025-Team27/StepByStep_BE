package com.dragon.stepbystep.exception;

public class BoardCommentNotFoundException extends RuntimeException {
    public BoardCommentNotFoundException(Long commentId) {
        super("댓글을 찾을 수 없습니다.");
    }
}