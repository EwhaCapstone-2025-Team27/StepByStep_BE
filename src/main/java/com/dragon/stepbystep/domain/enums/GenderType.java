package com.dragon.stepbystep.domain.enums;

public enum GenderType {
    F("여자"),
    M("남자");

    private final String displayName;

    GenderType(String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
