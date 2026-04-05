package com.example.backendspring.security;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.integration.BaseIntegrationTest;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@ActiveProfiles("test")
class DataOwnershipSecurityTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JobRepository jobRepository;

    private UUID maliciousUserId;
    private UUID victimUserId;
    private String maliciousUserToken;
    private String victimUserToken;
    private Job victimJob;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        jobRepository.deleteAll();
        maliciousUserId = UUID.randomUUID();
        victimUserId = UUID.randomUUID();
        maliciousUserToken = jwtService.generateToken(maliciousUserId);
        victimUserToken = jwtService.generateToken(victimUserId);

        victimJob = Job.builder()
                .userId(victimUserId)
                .text("sensitive voice data")
                .status(JobStatus.CREATED)
                .fileName("secret-recording.txt")
                .voiceId("uk-UA-OstapNeural")
                .build();
        jobRepository.save(victimJob);
    }

    @Nested
    class GetEndpointIDOR {

        @Test
        void testCannotGetOtherUserJob() throws Exception {
            mockMvc.perform(get("/api/v1/jobs/" + victimJob.getId())
                    .header("Authorization", "Bearer " + maliciousUserToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testJobListFiltersByUser() throws Exception {
            Job attackerJob = Job.builder()
                    .userId(maliciousUserId)
                    .text("attacker's data")
                    .status(JobStatus.CREATED)
                    .fileName("attacker-file.txt")
                    .build();
            jobRepository.save(attackerJob);

            mockMvc.perform(get("/api/v1/jobs")
                    .header("Authorization", "Bearer " + maliciousUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void testVictimCanOnlySeeSelfJobs() throws Exception {
            Job attackerJob = Job.builder()
                    .userId(maliciousUserId)
                    .text("attacker data")
                    .status(JobStatus.CREATED)
                    .fileName("attacker-file.txt")
                    .build();
            jobRepository.save(attackerJob);

            mockMvc.perform(get("/api/v1/jobs")
                    .header("Authorization", "Bearer " + victimUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    class DeleteEndpointIDOR {

        @Test
        void testCannotDeleteOtherUserJob() throws Exception {
            mockMvc.perform(delete("/api/v1/jobs/" + victimJob.getId())
                    .header("Authorization", "Bearer " + maliciousUserToken))
                    .andExpect(status().isNotFound());

            assert jobRepository.findById(victimJob.getId()).isPresent();
        }

        @Test
        void testCanDeleteOwnJob() throws Exception {
            Job userJob = Job.builder()
                    .userId(maliciousUserId)
                    .text("my job")
                    .status(JobStatus.CREATED)
                    .fileName("my-file.txt")
                    .build();
            Job savedJob = jobRepository.save(userJob);

            mockMvc.perform(delete("/api/v1/jobs/" + savedJob.getId())
                    .header("Authorization", "Bearer " + maliciousUserToken))
                    .andExpect(status().isOk());

            assert jobRepository.findById(savedJob.getId()).isEmpty();
        }
    }

    @Nested
    class DataIsolation {

        @Test
        void testMultipleUsersDataIsolation() throws Exception {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID user3 = UUID.randomUUID();

            String token1 = jwtService.generateToken(user1);
            String token2 = jwtService.generateToken(user2);
            String token3 = jwtService.generateToken(user3);

            Job job1 = Job.builder().userId(user1).text("user1 data").status(JobStatus.CREATED).fileName("f1.txt").build();
            Job job2 = Job.builder().userId(user2).text("user2 data").status(JobStatus.CREATED).fileName("f2.txt").build();
            Job job3 = Job.builder().userId(user3).text("user3 data").status(JobStatus.CREATED).fileName("f3.txt").build();

            jobRepository.saveAll(java.util.List.of(job1, job2, job3));

            // Each user should only see their own job
            mockMvc.perform(get("/api/v1/jobs")
                    .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/api/v1/jobs")
                    .header("Authorization", "Bearer " + token2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/api/v1/jobs")
                    .header("Authorization", "Bearer " + token3))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }
}

