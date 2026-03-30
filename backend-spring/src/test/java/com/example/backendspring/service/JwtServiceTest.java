package com.example.backendspring.service;

import com.example.backendspring.config.properties.JwtProperties;
import com.example.backendspring.support.TestResources;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        SecretKey secretKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        jwtService = new JwtService(jwtProperties, secretKey);
    }

    @Test
    void generateTokenCreatesSignedJwt() {
        when(jwtProperties.expirationDays()).thenReturn(7L);

        String token = jwtService.generateToken(TestResources.USER_ID);

        assertFalse(token.isBlank());
    }

    @Test
    void extractUserIdReturnsSameUserIdFromGeneratedToken() {
        when(jwtProperties.expirationDays()).thenReturn(7L);
        UUID userId = TestResources.USER_ID;

        String token = jwtService.generateToken(userId);
        UUID extracted = jwtService.extractUserId(token);

        assertEquals(userId, extracted);
    }

    @Test
    void extractUserIdThrowsForInvalidToken() {
        assertThrows(RuntimeException.class, () -> jwtService.extractUserId("invalid.token.value"));
    }
}

