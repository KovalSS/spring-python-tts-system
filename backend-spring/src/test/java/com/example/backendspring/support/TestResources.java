package com.example.backendspring.support;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.model.UpdateJobMessage;

import java.util.UUID;

public final class TestResources {

    public static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID JOB_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final String INPUT_QUEUE = "tts_requests";
    public static final String OUTPUT_QUEUE = "tts_responses";

    private TestResources() {
    }

    public static Job job(JobStatus status) {
        return Job.builder()
                .id(JOB_ID)
                .userId(USER_ID)
                .text("test text")
                .voiceId("uk-UA-OstapNeural")
                .rate("+0%")
                .pitch("+0Hz")
                .volume("+0%")
                .sourceFile("source/test.txt")
                .resultFile("result/test.mp3")
                .status(status)
                .fileName("test.txt")
                .build();
    }

    public static Job job(UUID userId, JobStatus status, String fileName) {
        return Job.builder()
                .userId(userId)
                .text("integration text")
                .voiceId("uk-UA-OstapNeural")
                .rate("+0%")
                .pitch("+0Hz")
                .volume("+0%")
                .sourceFile("source/" + fileName)
                .status(status)
                .fileName(fileName)
                .build();
    }

    public static UpdateJobMessage updateMessage(UUID jobId, String status, String resultFile) {
        UpdateJobMessage message = new UpdateJobMessage();
        message.setJobId(jobId);
        message.setStatus(status);
        message.setResultFile(resultFile);
        return message;
    }
}

