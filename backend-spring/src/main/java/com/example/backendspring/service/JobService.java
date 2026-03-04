package com.example.backendspring.service;

import com.example.backendspring.entity.Job;

import java.util.List;
import java.util.UUID;

public interface JobService {
    List<Job> getAllJobsByUserId(UUID userID);
    Job findById(UUID jobId, UUID userId);
    void deleteJob(UUID jobId, UUID userId);
}
