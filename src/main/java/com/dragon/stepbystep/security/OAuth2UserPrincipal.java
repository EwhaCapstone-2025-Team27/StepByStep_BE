package com.dragon.stepbystep.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

//@AllArgsConstructor
public class OAuth2UserPrincipal implements OAuth2User {
    private String UserEmail;
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;

    public OAuth2UserPrincipal(String userEmail, Map<String, Object> attributes,
                               Collection<? extends GrantedAuthority> authorities) {
        this.UserEmail = userEmail;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    @Override
    public String getName() {
        return UserEmail;
    }
}