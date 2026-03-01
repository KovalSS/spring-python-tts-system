package com.example.backendspring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config.jwt")
public record JwtProperties(String secret, long expirationDays) {
}
