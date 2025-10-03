package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.TokenBlacklist;
import com.dragon.stepbystep.repository.TokenBlacklistRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    public void blacklistToken(String token) {
        String tokenHash = hashToken(token);
        if (tokenBlacklistRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        Instant expiresAt = extractExpiration(token);
        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();
        tokenBlacklistRepository.save(tokenBlacklist);
    }

    public boolean isTokenBlacklisted(String token) {
        String tokenHash = hashToken(token);
        return tokenBlacklistRepository.existsByTokenHash(tokenHash);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                sb.append(String.format("%02x", hashByte));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private Instant extractExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().toInstant();
    }
}