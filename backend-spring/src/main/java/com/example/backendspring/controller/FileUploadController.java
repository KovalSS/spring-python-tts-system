package com.example.backendspring.controller;

import com.example.backendspring.entity.Job;
import com.example.backendspring.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file")
public class FileUploadController {

    private final Path rootLocation = Paths.get("doc-uploads");
    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file")MultipartFile file){

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            storageService.loadTextFile(file, UUID.randomUUID());
            return ResponseEntity.ok("Uploaded file: " + file.getOriginalFilename());

        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());

            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/file-list")
    public ResponseEntity<List<Job>> getAllFiles(){
        return ResponseEntity.ok(storageService.getAllFiles());
    }
}
