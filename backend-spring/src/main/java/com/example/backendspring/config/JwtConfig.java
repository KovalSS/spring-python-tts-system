package com.example.backendspring.config;

import com.example.backendspring.config.properties.JwtProperties;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {
    private final JwtProperties jwtProperties;

    @Bean
    public SecretKey secretKey(){
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }
}
