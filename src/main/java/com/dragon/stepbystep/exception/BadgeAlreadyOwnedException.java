package com.dragon.stepbystep.exception;

public class BadgeAlreadyOwnedException extends RuntimeException {
    public BadgeAlreadyOwnedException() {
        super("이미 소유한 배지입니다.");
    }
}