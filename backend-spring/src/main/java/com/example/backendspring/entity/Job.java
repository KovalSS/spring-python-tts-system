package com.example.backendspring.entity;

import jakarta.persistence.*;
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

    private String text;
    private String voiceId;

    private String sourceFile;
    private String resultFile;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private String fileName;
}