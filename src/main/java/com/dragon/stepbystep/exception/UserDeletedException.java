package com.dragon.stepbystep.exception;

public class UserDeletedException extends RuntimeException {
    public UserDeletedException() {
        super("탈퇴한 계정입니다.");
    }
}
