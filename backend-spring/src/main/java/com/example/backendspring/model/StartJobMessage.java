package com.example.backendspring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class StartJobMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -5614025147673846636L;

    private String jobId;
    private String sourcePath;
}
