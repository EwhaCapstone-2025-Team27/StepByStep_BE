package com.dragon.stepbystep.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
public class OAuth2UserPrincipal implements OAuth2User {

    @Getter
    private final String userId; // ← 명확히 userId 보관
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes == null ? Collections.emptyMap() : attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? Collections.emptyList() : authorities;
    }

    @Override
    public String getName() {
        return userId; // ← Authentication.getName() == userId 보장
    }
}