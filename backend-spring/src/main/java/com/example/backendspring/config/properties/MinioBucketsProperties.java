package com.example.backendspring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config.minio.buckets")
public record MinioBucketsProperties(String text, String speech) {
}
