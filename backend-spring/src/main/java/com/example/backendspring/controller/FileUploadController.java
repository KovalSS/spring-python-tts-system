package com.example.backendspring.controller;

import com.example.backendspring.entity.Job;
import com.example.backendspring.security.UserContext;
import com.example.backendspring.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file")
public class FileUploadController {

    private final StorageService storageService;

    private final UserContext userContext;

    @PostMapping("/upload")
    public ResponseEntity<Job> handleFileUpload(@RequestParam("file")MultipartFile file){

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

    @GetMapping("/file-list")
    public ResponseEntity<List<Job>> getAllFiles(){
        log.info("User with id {} request job list", userContext.getUserId());
        return ResponseEntity.ok(storageService.getAllFiles());
    }
}
