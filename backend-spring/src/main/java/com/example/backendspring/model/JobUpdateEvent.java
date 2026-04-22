package com.example.backendspring.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class JobUpdateEvent {
    private UUID jobId;
    private String status;
    private String resultFile;
}

