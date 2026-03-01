package com.example.backendspring.controller;

import com.example.backendspring.entity.FileEntity;
import com.example.backendspring.entity.FileStatus;
import com.example.backendspring.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final FileRepository fileRepository;

    // CREATE: Створити нове завдання на генерацію аудіо
    @PostMapping
    public ResponseEntity<FileEntity> createJob(@RequestBody FileEntity jobRequest) {
        jobRequest.setStatus(FileStatus.CREATED); // Початковий статус
        FileEntity savedJob = fileRepository.save(jobRequest);
        return ResponseEntity.ok(savedJob);
    }

    // READ: Отримати всі завдання
    @GetMapping
    public ResponseEntity<List<FileEntity>> getAllJobs() {
        return ResponseEntity.ok(fileRepository.findAll());
    }

    // READ: Отримати одне завдання за ID
    @GetMapping("/{id}")
    public ResponseEntity<FileEntity> getJobById(@PathVariable UUID id) {
        return fileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE: Оновити статус завдання
    @PutMapping("/{id}/status")
    public ResponseEntity<FileEntity> updateJobStatus(@PathVariable UUID id, @RequestParam FileStatus status) {
        return fileRepository.findById(id).map(job -> {
            job.setStatus(status);
            return ResponseEntity.ok(fileRepository.save(job));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Видалити завдання
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        if (fileRepository.existsById(id)) {
            fileRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}