package com.dragon.stepbystep.exception;

public class InsufficientPointsException extends RuntimeException {
    public InsufficientPointsException() {
        super("포인트가 부족합니다!");
    }
}