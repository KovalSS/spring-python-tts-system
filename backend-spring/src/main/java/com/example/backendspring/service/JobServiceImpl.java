package com.example.backendspring.service;

import com.example.backendspring.controller.exceptions.NotFoundException;
import com.example.backendspring.entity.Job;
import com.example.backendspring.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;

    @Override
    public List<Job> getAllJobsByUserId(UUID userID) {
        return jobRepository.findAllByUserId(userID);
    }

    @Override
    public Job findById(UUID jobId, UUID userId) {

        Optional<Job> found = jobRepository.findByIdAndUserId(jobId, userId);
        return found.orElseThrow(NotFoundException::new);
    }

    @Override
    public void deleteJob(UUID jobId, UUID userId) {
        Job found = findById(jobId, userId);
        jobRepository.delete(found);
    }
}
