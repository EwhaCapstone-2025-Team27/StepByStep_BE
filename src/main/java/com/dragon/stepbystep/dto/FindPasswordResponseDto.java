package com.dragon.stepbystep.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FindPasswordResponseDto {

    private String message;

    private String email;

    public FindPasswordResponseDto(String message, String email) {
        this.message = message;
        this.email = email;
    }
}
