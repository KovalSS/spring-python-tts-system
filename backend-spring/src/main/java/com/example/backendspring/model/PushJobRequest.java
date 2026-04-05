package com.example.backendspring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class PushJobRequest {
    private String voiceId;
    private String rate;
    private String pitch;
    private String volume;
}

