package com.example.backendspring.service;

import com.example.backendspring.controller.exceptions.NotFoundException;
import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.support.TestResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void getAllJobsByUserIdReturnsRepositoryResult() {
        Job job = TestResources.job(JobStatus.CREATED);
        when(jobRepository.findAllByUserId(eq(TestResources.USER_ID))).thenReturn(List.of(job));

        List<Job> result = jobService.getAllJobsByUserId(TestResources.USER_ID);

        assertEquals(1, result.size());
        assertEquals(job, result.get(0));
    }

    @Test
    void findByIdReturnsJobWhenOwnedByUser() {
        Job job = TestResources.job(JobStatus.CREATED);
        when(jobRepository.findByIdAndUserId(eq(TestResources.JOB_ID), eq(TestResources.USER_ID)))
                .thenReturn(Optional.of(job));

        Job result = jobService.findById(TestResources.JOB_ID, TestResources.USER_ID);

        assertEquals(job, result);
    }

    @Test
    void findByIdThrowsWhenJobIsMissing() {
        when(jobRepository.findByIdAndUserId(eq(TestResources.JOB_ID), eq(TestResources.USER_ID)))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> jobService.findById(TestResources.JOB_ID, TestResources.USER_ID));
    }

    @Test
    void deleteJobRemovesFoundJob() {
        Job job = TestResources.job(JobStatus.PROCESSING);
        when(jobRepository.findByIdAndUserId(eq(TestResources.JOB_ID), eq(TestResources.USER_ID)))
                .thenReturn(Optional.of(job));

        jobService.deleteJob(TestResources.JOB_ID, TestResources.USER_ID);

        verify(jobRepository).delete(eq(job));
    }
}

