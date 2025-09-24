package com.dragon.stepbystep.domain.enums;

public enum UserStatus {
    ACTIVE("활동"),
    DELETED("탈퇴");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
