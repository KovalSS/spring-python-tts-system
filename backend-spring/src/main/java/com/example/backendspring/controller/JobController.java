package com.example.backendspring.controller;

import com.example.backendspring.entity.Job;
import com.example.backendspring.model.PushJobRequest;
import com.example.backendspring.model.StartJobMessage;
import com.example.backendspring.security.UserContext;
import com.example.backendspring.service.JobService;
import com.example.backendspring.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final UserContext userContext;
    private final MessageService messageService;

//    @PostMapping
//    public ResponseEntity<Job> createJob(@RequestBody Job jobRequest) {
//        jobRequest.setStatus(JobStatus.CREATED);
//        Job savedJob = jobRepository.save(jobRequest);
//        return ResponseEntity.ok(savedJob);
//    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobsByUserId(userContext.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.findById(id, userContext.getUserId()));
    }
//
//    @PutMapping("/{id}/status")
//    public ResponseEntity<Job> updateJobStatus(@PathVariable UUID id, @RequestParam JobStatus status) {
//        return jobRepository.findById(id).map(job -> {
//            job.setStatus(status);
//            return ResponseEntity.ok(jobRepository.save(job));
//        }).orElse(ResponseEntity.notFound().build());
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        jobService.deleteJob(id, userContext.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/push")
    public ResponseEntity<StartJobMessage> publishJob(
            @PathVariable UUID id,
            @RequestBody(required = false) PushJobRequest params
    ){
        Job found = jobService.findById(id, userContext.getUserId());
        
        if (params != null) {
            if (params.getVoiceId() != null && !params.getVoiceId().isBlank()) {
                found.setVoiceId(params.getVoiceId());
            }
            if (params.getRate() != null && !params.getRate().isBlank()) {
                found.setRate(params.getRate());
            }
            if (params.getPitch() != null && !params.getPitch().isBlank()) {
                found.setPitch(params.getPitch());
            }
            if (params.getVolume() != null && !params.getVolume().isBlank()) {
                found.setVolume(params.getVolume());
            }
        }
        
        try{
            StartJobMessage message =  messageService.sendJobToQueue(found);
            return ResponseEntity.ok(message);
        } catch (Exception e){
            log.error("Error during publishing message");
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

//     @PostMapping("/{id}/start")
//     public ResponseEntity<Job> startExistingJob(@PathVariable UUID id) {
//         return jobRepository.findById(id).map(job -> {
//             if (!job.getUserId().equals(userContext.getUserId())) {
//                 return ResponseEntity.status(403).body(job); // Forbidden
//             }

//             if (job.getStatus() == JobStatus.ERROR) {
//                 job.setStatus(JobStatus.CREATED);
//             }

//             messageService.sendJobToQueue(job);
//             return ResponseEntity.ok(job);
//         }).orElse(ResponseEntity.notFound().build());
//     }
}