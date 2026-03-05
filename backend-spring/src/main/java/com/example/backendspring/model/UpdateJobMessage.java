package com.example.backendspring.model;

import com.example.backendspring.entity.JobStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateJobMessage {
    private UUID jobId;
    private String resultFile;
    private String status;
}
