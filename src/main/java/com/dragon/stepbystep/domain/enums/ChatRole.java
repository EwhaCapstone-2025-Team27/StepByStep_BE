package com.dragon.stepbystep.domain.enums;

public enum ChatRole {
    USER("질문자"),
    ASSISTANT("챗봇"),
    SYSTEM("시스템");

    private final String displayName;

    ChatRole(String value) {
        this.displayName = value;
    }

    @Override
    public String toString() {
        return displayName;
    }
}