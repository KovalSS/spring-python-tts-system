package com.example.backendspring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="spring.rabbitmq.queue")
public record RabbitMQProperties(String inputQueue, String outputQueue) {
}
