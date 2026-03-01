package com.example.backendspring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="spring.rabbitmq.queue")
public record RabbitMQProperties(String inputQueue, String outputQueue) {
}
