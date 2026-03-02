package com.example.backendspring.service;

import com.example.backendspring.config.properties.RabbitMQProperties;
import com.example.backendspring.model.StartJobMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final RabbitMQProperties properties;
    private final RabbitTemplate template;

    public void pushJob(StartJobMessage job){
        template.convertAndSend(properties.outputQueue(), job);
    }
}
