package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;
    private boolean forcePasswordChange;

    public TokenDto(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, false);
    }

    public TokenDto(String accessToken, String refreshToken, boolean forcePasswordChange) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.forcePasswordChange = forcePasswordChange;
    }
}