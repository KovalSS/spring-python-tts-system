package com.example.backendspring.model;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class JobResponseDto {
    UUID id;
    JobStatus status;
    String text;
    String voiceId;
    String rate;
    String pitch;
    String volume;
    String fileName;

    public static JobResponseDto from(Job job) {
        return JobResponseDto.builder()
                .id(job.getId())
                .status(job.getStatus())
                .text(job.getText())
                .voiceId(job.getVoiceId())
                .rate(job.getRate())
                .pitch(job.getPitch())
                .volume(job.getVolume())
                .fileName(job.getFileName())
                .build();
    }
}

