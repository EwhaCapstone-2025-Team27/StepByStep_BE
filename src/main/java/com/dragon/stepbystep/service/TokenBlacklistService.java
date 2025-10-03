package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.TokenBlacklist;
import com.dragon.stepbystep.repository.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    public void blacklistToken(String token) {
        if (!tokenBlacklistRepository.existsByToken(token)) {
            TokenBlacklist tokenBlacklist = new TokenBlacklist(null, null, null);
            tokenBlacklistRepository.save(tokenBlacklist);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}