package com.example.backendspring.service;

import com.example.backendspring.config.properties.RabbitMQProperties;
import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.model.StartJobMessage;
import com.example.backendspring.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final RabbitMQProperties properties;
    private final RabbitTemplate rabbitTemplate;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public void sendJobToQueue(Job job) {
        if (job.getStatus() == JobStatus.QUEUED ||
                job.getStatus() == JobStatus.PROCESSING ||
                job.getStatus() == JobStatus.DONE) {
            log.warn("Job {} is already in status {}. Skipping RabbitMQ.", job.getId(), job.getStatus());
            return;
        }

        try {
            StartJobMessage message = StartJobMessage.builder()
                    .jobId(job.getId().toString())
                    .text(job.getText())
                    .sourcePath(job.getSourceFile())
                    .voiceId(job.getVoiceId() != null ? job.getVoiceId() : "uk-UA-OstapNeural")
                    .rate(job.getRate() != null ? job.getRate() : "+0%")
                    .pitch(job.getPitch() != null ? job.getPitch() : "+0Hz")
                    .volume(job.getVolume() != null ? job.getVolume() : "+0%")
                    .build();
            rabbitTemplate.convertAndSend(properties.inputQueue(), message);

            job.setStatus(JobStatus.QUEUED);
            jobRepository.save(job);

            log.info("Job {} sent to RabbitMQ", job.getId());
        } catch (Exception e) {
            log.error("Error sending to RabbitMQ", e);
        }
    }

    // Listen for responses from Python worker
    @RabbitListener(queues = "${spring.rabbitmq.queue.output-queue}")
    public void receiveStatusUpdate(String message) {
        try {
            Map<String, String> data = objectMapper.readValue(message, new TypeReference<>() {});
            UUID jobId = UUID.fromString(data.get("jobId"));
            String statusStr = data.get("status");
            String resultFile = data.get("resultFile");

            jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus(JobStatus.valueOf(statusStr));
                if (resultFile != null) {
                    job.setResultFile(resultFile);
                }
                jobRepository.save(job);
                log.info("Updated status for job {}: {}", jobId, statusStr);
            });
        } catch (Exception e) {
            log.error("Error processing message from RabbitMQ", e);
        }
    }
}
