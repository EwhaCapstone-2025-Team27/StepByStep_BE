package com.dragon.stepbystep.exception;

public class BadgeNotFoundException extends RuntimeException {
    public BadgeNotFoundException() {
        super("배지를 찾을 수 없습니다.");
    }
}