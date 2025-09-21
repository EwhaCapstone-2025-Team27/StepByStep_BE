package com.dragon.stepbystep.domain.enums;

public enum AttemptStatus {
    IN_PROGRESS("진행중"),
    SUBMITTED("제출됨"),
    CANCELLED("취소됨");

    private final String description;

    AttemptStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
