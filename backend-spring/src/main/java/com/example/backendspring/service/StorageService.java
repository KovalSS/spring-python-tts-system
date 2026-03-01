package com.example.backendspring.service;


import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.repository.JobRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final String TEXT_BUCKET_NAME = "text-files";
    private final MinioClient minioClient;
    private final JobRepository jobRepository;

    public void loadTextFile(MultipartFile file, UUID userId){
        try (InputStream inputStream = file.getInputStream()) {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(TEXT_BUCKET_NAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(TEXT_BUCKET_NAME).build());
            }

            String fileName = file.getOriginalFilename();

            String path = UUID.randomUUID() + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(TEXT_BUCKET_NAME)
                            .object(path)
                            .contentType(file.getContentType())
                            .stream(inputStream, file.getSize(), -1)
                            .build()
            );

            Job job = Job.builder()
                    .fileName(fileName)
                    .sourceFile(path)
                    .userId(userId)
                    .status(JobStatus.NEW)
                    .build();

            jobRepository.save(job);
        } catch (Exception e) {
            log.error("Error occurred during loading file {}", file.getName());
            log.error(e.getMessage());
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    public List<Job> getAllFiles(){
        return jobRepository.findAll();
    }


}
