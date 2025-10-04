package com.dragon.stepbystep.exception;

public class BoardNotFoundException extends RuntimeException {
  public BoardNotFoundException(Long boardId) {
    super("게시글을 찾을 수 없습니다.");
  }
}