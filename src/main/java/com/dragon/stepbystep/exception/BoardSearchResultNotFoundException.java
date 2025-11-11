package com.dragon.stepbystep.exception;

public class BoardSearchResultNotFoundException extends RuntimeException {
    public BoardSearchResultNotFoundException() {
        super("검색 결과가 없습니다.");
    }
}
