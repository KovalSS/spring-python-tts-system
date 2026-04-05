package com.example.backendspring.service;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.integration.BaseIntegrationTest;
import com.example.backendspring.model.UpdateJobMessage;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.support.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MessageServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MessageService messageService;


    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void clearData() {
        jobRepository.deleteAll();
    }

    @Test
    void sendJobToQueueEnqueuesMessageAndUpdatesStatus() {
        UUID userId = UUID.randomUUID();
        Job job = jobRepository.save(TestResources.job(userId, JobStatus.CREATED, "send-test.txt"));

        messageService.sendJobToQueue(job);

        Job updated = jobRepository.findById(job.getId()).orElseThrow();
        assertEquals(JobStatus.QUEUED, updated.getStatus());
        assertNotNull(updated.getId());
    }

    @Test
    void receiveStatusUpdateProcessesMessageAndPersistsChanges() {
        UUID userId = UUID.randomUUID();
        Job job = jobRepository.save(TestResources.job(userId, JobStatus.PROCESSING, "receive-test.txt"));

        UpdateJobMessage message = new UpdateJobMessage();
        message.setJobId(job.getId());
        message.setStatus("DONE");
        message.setResultFile("speech/output.mp3");

        messageService.receiveStatusUpdate(message);

        Job updated = jobRepository.findById(job.getId()).orElseThrow();
        assertEquals(JobStatus.DONE, updated.getStatus());
        assertEquals("speech/output.mp3", updated.getResultFile());
    }

    @Test
    void sendJobToQueueSkipsAlreadyProcessingJob() {
        UUID userId = UUID.randomUUID();
        Job job = jobRepository.save(TestResources.job(userId, JobStatus.PROCESSING, "skip-test.txt"));

        messageService.sendJobToQueue(job);

        Job unchanged = jobRepository.findById(job.getId()).orElseThrow();
        assertEquals(JobStatus.PROCESSING, unchanged.getStatus());
    }
}

