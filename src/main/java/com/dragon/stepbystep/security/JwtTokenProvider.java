package com.dragon.stepbystep.security;

import com.dragon.stepbystep.exception.JwtAuthenticationException;
import com.dragon.stepbystep.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Setter
public class JwtTokenProvider {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    // access 토큰 유효 기간 30분
    private long accessTokenValidityInMilliseconds = 1800000;
    // refresh 토큰 유효 기간 7일
    private long refreshTokenValidityInMilliseconds = 604800000;

    // access 토큰 생성
    public String createAccessToken(Long id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        // JWT 토큰 생성
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // refresh 토큰 생성
    public String createRefreshToken(Long id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // refresh 토큰 검증 및 새로운 access 토큰 발급
    public String refreshAccessToken(String refreshToken) {
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new JwtAuthenticationException("로그인되지 않은 사용자입니다.");
        }

        try {
            String userId = getUserIdFromToken(refreshToken);
            return createAccessToken(Long.valueOf(userId));
        } catch (Exception e) {
            throw new JwtAuthenticationException("로그인되지 않은 사용자입니다.");
        }
    }

    // 토큰에서 사용자 아이디 추출
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        return !isTokenExpired(token)&&!tokenBlacklistService.isTokenBlacklisted(token);
    }
}
