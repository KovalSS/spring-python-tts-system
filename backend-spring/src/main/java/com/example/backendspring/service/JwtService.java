package com.example.backendspring.service;

import com.example.backendspring.config.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public String generateToken(UUID userId) {
        Date expiration = new Date(System.currentTimeMillis() + jwtProperties.expirationDays() * 24 * 60 * 60 * 1000);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public UUID extractUserId(String token) {
        String userId = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(userId);
    }
}
