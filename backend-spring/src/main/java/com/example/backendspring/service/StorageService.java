package com.example.backendspring.service;


import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final String TEXT_BUCKET_NAME = "text-files";
    private final MinioClient minioClient;

    public void loadTextFile(MultipartFile file){
        try (InputStream inputStream = file.getInputStream()) {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(TEXT_BUCKET_NAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(TEXT_BUCKET_NAME).build());
            }
            ObjectWriteResponse response =  minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(TEXT_BUCKET_NAME)
                            .object(file.getOriginalFilename()+ UUID.randomUUID())
                            .contentType(file.getContentType())
                            .stream(inputStream, file.getSize(), -1)
                            .build()
            );

        } catch (Exception e) {
            log.error("Error occurred during loading file {}", file.getName());
            log.error(e.getMessage());
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }
}
