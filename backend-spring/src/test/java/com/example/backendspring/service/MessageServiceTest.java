package com.example.backendspring.service;

import com.example.backendspring.config.properties.RabbitMQProperties;
import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.model.StartJobMessage;
import com.example.backendspring.model.UpdateJobMessage;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.support.TestResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private RabbitMQProperties properties;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendJobToQueueSkipsWhenStatusIsQueuedProcessingOrDone() {
        Job queued = TestResources.job(JobStatus.QUEUED);
        Job processing = TestResources.job(JobStatus.PROCESSING);
        Job done = TestResources.job(JobStatus.DONE);

        assertNull(messageService.sendJobToQueue(queued));
        assertNull(messageService.sendJobToQueue(processing));
        assertNull(messageService.sendJobToQueue(done));

        verifyNoInteractions(rabbitTemplate);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void sendJobToQueueSendsMessageAndUpdatesStatusWithDefaults() {
        Job job = TestResources.job(JobStatus.CREATED);
        job.setText(null);
        job.setVoiceId(null);
        job.setRate(null);
        job.setPitch(null);
        job.setVolume(null);

        when(properties.inputQueue()).thenReturn(TestResources.INPUT_QUEUE);

        StartJobMessage message = messageService.sendJobToQueue(job);

        assertNotNull(message);
        assertEquals(job.getId().toString(), message.getJobId());
        assertEquals(job.getSourceFile(), message.getSourcePath());
        assertEquals("uk-UA-OstapNeural", message.getVoiceId());
        assertEquals("+0%", message.getRate());
        assertEquals("+0Hz", message.getPitch());
        assertEquals("+0%", message.getVolume());
        assertEquals(JobStatus.QUEUED, job.getStatus());

        verify(rabbitTemplate).convertAndSend(eq(TestResources.INPUT_QUEUE), eq(message));
        verify(jobRepository).save(eq(job));
    }

    @Test
    void receiveStatusUpdateAppliesResultFileWhenJobExists() {
        Job job = TestResources.job(JobStatus.PROCESSING);
        UpdateJobMessage update = TestResources.updateMessage(TestResources.JOB_ID, "DONE", "speech/output.mp3");

        when(jobRepository.findById(eq(TestResources.JOB_ID))).thenReturn(Optional.of(job));

        messageService.receiveStatusUpdate(update);

        assertEquals(JobStatus.DONE, job.getStatus());
        assertEquals("speech/output.mp3", job.getResultFile());
        verify(jobRepository).save(eq(job));
    }

    @Test
    void receiveStatusUpdateDoesNothingWhenJobDoesNotExist() {
        UpdateJobMessage update = TestResources.updateMessage(TestResources.JOB_ID, "DONE", "speech/output.mp3");
        when(jobRepository.findById(eq(TestResources.JOB_ID))).thenReturn(Optional.empty());

        messageService.receiveStatusUpdate(update);

        verify(jobRepository, never()).save(eq(TestResources.job(JobStatus.DONE)));
    }

    @Test
    void receiveStatusUpdateIgnoresInvalidStatus() {
        UpdateJobMessage update = TestResources.updateMessage(TestResources.JOB_ID, "NOT_A_STATUS", null);

        messageService.receiveStatusUpdate(update);

        verify(jobRepository, never()).findById(eq(TestResources.JOB_ID));
    }
}

