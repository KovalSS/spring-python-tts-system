package com.example.backendspring.controller;

import com.example.backendspring.model.AuthResponse;
import com.example.backendspring.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final JwtService jwtService;

    @PostMapping("/anonymous")
    public AuthResponse anonymous(){
        UUID userId = UUID.randomUUID();
        return new AuthResponse(jwtService.generateToken(userId), userId);
    }
}
