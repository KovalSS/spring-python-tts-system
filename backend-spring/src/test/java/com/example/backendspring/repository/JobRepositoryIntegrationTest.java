package com.example.backendspring.repository;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.integration.PostgresContainerSupport;
import com.example.backendspring.support.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JobRepositoryIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void clearData() {
        jobRepository.deleteAll();
    }

    @Test
    void findAllByUserIdReturnsOnlyOwnedJobs() {
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();

        jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "owner-a.txt"));
        jobRepository.save(TestResources.job(owner, JobStatus.PROCESSING, "owner-b.txt"));
        jobRepository.save(TestResources.job(stranger, JobStatus.DONE, "stranger.txt"));

        List<Job> jobs = jobRepository.findAllByUserId(owner);

        assertEquals(2, jobs.size());
        assertTrue(jobs.stream().allMatch(job -> owner.equals(job.getUserId())));
    }

    @Test
    void findByIdAndUserIdReturnsResultOnlyForOwner() {
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();

        Job saved = jobRepository.save(TestResources.job(owner, JobStatus.CREATED, "owned.txt"));

        Optional<Job> ownerView = jobRepository.findByIdAndUserId(saved.getId(), owner);
        Optional<Job> strangerView = jobRepository.findByIdAndUserId(saved.getId(), stranger);

        assertTrue(ownerView.isPresent());
        assertTrue(strangerView.isEmpty());
    }
}

