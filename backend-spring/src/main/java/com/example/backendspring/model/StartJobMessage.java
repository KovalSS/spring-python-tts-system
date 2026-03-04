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

    private String jobId;
    private String sourcePath;

    private String text;
    private String voiceId;
    private String rate;
    private String pitch;
    private String volume;
}
