package com.example.backendspring.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    private String sourceFile;
    private String resultFile;

    private JobStatus status;
    private String fileName;
}
