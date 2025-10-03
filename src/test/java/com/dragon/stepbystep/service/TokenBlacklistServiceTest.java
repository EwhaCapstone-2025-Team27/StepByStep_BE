package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.TokenBlacklist;
import com.dragon.stepbystep.repository.TokenBlacklistRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    private static final String SECRET_KEY = "test-secret-key";

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(tokenBlacklistRepository);
        ReflectionTestUtils.setField(tokenBlacklistService, "secretKey", SECRET_KEY);
    }

    @Test
    void blacklistToken_blacklistsAccessToken() {
        Instant issuedAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant expiresAt = issuedAt.plusSeconds(1800);
        String accessToken = createToken(issuedAt, expiresAt);
        String expectedHash = hash(accessToken);

        when(tokenBlacklistRepository.existsByTokenHash(expectedHash)).thenReturn(false);

        tokenBlacklistService.blacklistToken(accessToken);

        verify(tokenBlacklistRepository).existsByTokenHash(expectedHash);

        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository).save(captor.capture());
        TokenBlacklist saved = captor.getValue();

        assertThat(saved.getTokenHash()).isEqualTo(expectedHash);
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        verifyNoMoreInteractions(tokenBlacklistRepository);
    }

    @Test
    void blacklistToken_blacklistsRefreshToken() {
        Instant issuedAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant expiresAt = issuedAt.plusSeconds(604800);
        String refreshToken = createToken(issuedAt, expiresAt);
        String expectedHash = hash(refreshToken);

        when(tokenBlacklistRepository.existsByTokenHash(expectedHash)).thenReturn(false);

        tokenBlacklistService.blacklistToken(refreshToken);

        verify(tokenBlacklistRepository).existsByTokenHash(expectedHash);

        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository).save(captor.capture());
        TokenBlacklist saved = captor.getValue();

        assertThat(saved.getTokenHash()).isEqualTo(expectedHash);
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        verifyNoMoreInteractions(tokenBlacklistRepository);
    }

    @Test
    void isTokenBlacklisted_checksHashAgainstRepository() {
        Instant issuedAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant expiresAt = issuedAt.plusSeconds(1800);
        String token = createToken(issuedAt, expiresAt);
        String expectedHash = hash(token);

        when(tokenBlacklistRepository.existsByTokenHash(expectedHash)).thenReturn(true);

        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        assertThat(result).isTrue();
        verify(tokenBlacklistRepository).existsByTokenHash(expectedHash);
        verifyNoMoreInteractions(tokenBlacklistRepository);
    }

    private String createToken(Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .setSubject("1")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                sb.append(String.format("%02x", hashByte));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}