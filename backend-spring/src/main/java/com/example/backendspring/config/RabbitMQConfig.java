package com.example.backendspring.config;

import com.example.backendspring.config.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RabbitMQConfig {

    private final RabbitMQProperties properties;

    @Bean
    public Queue inputQueue(){
        return new Queue(properties.inputQueue());
    }

    @Bean
    public Queue outputQueue(){
        return new Queue(properties.outputQueue());
    }

}
