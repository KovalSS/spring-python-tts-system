package com.example.backendspring.config;

import com.example.backendspring.config.properties.JwtProperties;
import com.example.backendspring.config.properties.MinioBucketsProperties;
import com.example.backendspring.config.properties.MinioProperties;
import com.example.backendspring.config.properties.RabbitMQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RabbitMQProperties.class, JwtProperties.class, MinioProperties.class, MinioBucketsProperties.class})
public class AppConfig {
}
