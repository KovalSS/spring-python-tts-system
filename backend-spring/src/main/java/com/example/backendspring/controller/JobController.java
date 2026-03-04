package com.example.backendspring.controller;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.security.UserContext;
import com.example.backendspring.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;
    private final MessageService messageService; // Додали сервіс
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job jobRequest) {
        jobRequest.setStatus(JobStatus.CREATED);

        jobRequest.setUserId(userContext.getUserId());

        Job savedJob = jobRepository.save(jobRequest);
        messageService.sendJobToQueue(savedJob);
        return ResponseEntity.ok(savedJob);
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable UUID id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Job> updateJobStatus(@PathVariable UUID id, @RequestParam JobStatus status) {
        return jobRepository.findById(id).map(job -> {
            job.setStatus(status);
            return ResponseEntity.ok(jobRepository.save(job));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Job> startExistingJob(@PathVariable UUID id) {
        return jobRepository.findById(id).map(job -> {
            if (!job.getUserId().equals(userContext.getUserId())) {
                return ResponseEntity.status(403).body(job); // Forbidden
            }

            if (job.getStatus() == JobStatus.ERROR) {
                job.setStatus(JobStatus.CREATED);
            }

            messageService.sendJobToQueue(job);
            return ResponseEntity.ok(job);
        }).orElse(ResponseEntity.notFound().build());
    }
}