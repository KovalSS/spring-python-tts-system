package com.example.backendspring.security;

import com.example.backendspring.integration.PostgresContainerSupport;
import com.example.backendspring.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@ActiveProfiles("test")
@DisplayName("JWT Filter Security Tests")
class JwtFilterSecurityTest extends PostgresContainerSupport {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String validToken;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        userId = UUID.randomUUID();
        validToken = jwtService.generateToken(userId);
    }

    @Test
    @DisplayName("Should reject request with missing Authorization header")
    void testMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject token with corrupted signature")
    void testCorruptedTokenSignature() throws Exception {
        String corruptedToken = validToken.substring(0, validToken.length() - 1) + "X";
        
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + corruptedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject token with missing segment")
    void testTokenWithMissingSegment() throws Exception {
        String malformedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0";
        
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + malformedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should be case-sensitive for Bearer scheme")
    void testBearerSchemeCaseSensitivity() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "BEARER " + validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should accept Bearer with exact case")
    void testBearerWithExactCase() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should clear security context on invalid token")
    void testSecurityContextClearedOnInvalidToken() throws Exception {
        // First request with invalid token
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());

        // Second request with valid token should still work
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject 'null' string as token")
    void testNullStringToken() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer null"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject 'undefined' string as token")
    void testUndefinedStringToken() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer undefined"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should extract correct user ID from valid JWT token")
    void testValidTokenExtraction() {
        UUID extracted = jwtService.extractUserId(validToken);
        assert extracted.equals(userId);
    }

    @Test
    @DisplayName("Should fail to extract user ID from invalid token")
    void testInvalidTokenRejection() {
        try {
            jwtService.extractUserId("invalid.token.format");
            throw new AssertionError("Should have thrown exception");
        } catch (Exception e) {
            assert e.getMessage() != null;
        }
    }
}

