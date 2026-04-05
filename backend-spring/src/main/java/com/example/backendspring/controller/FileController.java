package com.example.backendspring.controller;

import com.example.backendspring.entity.Job;
import com.example.backendspring.security.UserContext;
import com.example.backendspring.service.JobService;
import com.example.backendspring.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    private final StorageService storageService;
    private final JobService jobService;
    private final UserContext userContext;

    @PostMapping("/upload")
    public ResponseEntity<Job> handleFileUpload(
            @RequestParam("file") MultipartFile file
    ){

        if (file.isEmpty()) {
            throw new IllegalArgumentException();
        }
        try {
            Job job = storageService.loadTextFile(file, userContext.getUserId());
            return ResponseEntity.ok(job);

        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());

            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/download/{jobId}")
    public ResponseEntity<Resource> getAllFiles(@PathVariable("jobId") @NonNull UUID jobId){

        Job job = jobService.findById(jobId, userContext.getUserId());

        try{
            InputStream resultFile = storageService.downloadFile(job);
            Resource resource = new InputStreamResource(resultFile);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + job.getResultFile());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }
}
