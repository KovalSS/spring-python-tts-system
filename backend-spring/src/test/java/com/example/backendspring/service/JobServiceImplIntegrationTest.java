package com.example.backendspring.service;

import com.example.backendspring.controller.exceptions.NotFoundException;
import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.integration.BaseIntegrationTest;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.support.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JobServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void clearData() {
        jobRepository.deleteAll();
    }

    @Test
    void getAllJobsByUserIdReturnsOnlyOwnedRows() {
        UUID owner = UUID.randomUUID();

        jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "a.txt"));
        jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "b.txt"));
        jobRepository.save(TestResources.job(UUID.randomUUID(), JobStatus.CREATED, "foreign.txt"));

        List<Job> jobs = jobService.getAllJobsByUserId(owner);

        assertEquals(2, jobs.size());
        assertTrue(jobs.stream().allMatch(job -> owner.equals(job.getUserId())));
    }

    @Test
    void findByIdReturnsOwnedJob() {
        UUID owner = UUID.randomUUID();
        Job saved = jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "find.txt"));

        Job found = jobService.findById(saved.getId(), owner);

        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void findByIdThrowsForAnotherUser() {
        UUID owner = UUID.randomUUID();
        UUID anotherUser = UUID.randomUUID();
        Job saved = jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "hidden.txt"));

        assertThrows(NotFoundException.class, () -> jobService.findById(saved.getId(), anotherUser));
    }

    @Test
    void deleteJobRemovesOwnedRecord() {
        UUID owner = UUID.randomUUID();
        Job saved = jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "delete.txt"));

        jobService.deleteJob(saved.getId(), owner);

        assertTrue(jobRepository.findById(saved.getId()).isEmpty());
    }
}

