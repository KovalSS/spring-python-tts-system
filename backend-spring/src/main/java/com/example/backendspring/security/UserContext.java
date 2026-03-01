package com.example.backendspring.security;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Data
@Component
@RequestScope
public class UserContext {
    private UUID userId;
}
