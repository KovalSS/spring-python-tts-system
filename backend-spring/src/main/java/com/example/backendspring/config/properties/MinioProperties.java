package com.example.backendspring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config.minio")
public record MinioProperties(String url, String user, String password) {
}
