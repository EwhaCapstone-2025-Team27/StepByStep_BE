package com.dragon.stepbystep.domain.enums;

public enum BoardSearchType {
    ALL,
    CONTENT,
    NICKNAME;

    public static BoardSearchType from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return BoardSearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ALL;
        }
    }
}