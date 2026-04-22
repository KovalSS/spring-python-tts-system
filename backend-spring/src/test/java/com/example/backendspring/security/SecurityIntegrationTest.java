package com.example.backendspring.security;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.integration.BaseIntegrationTest;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JobRepository jobRepository;

    private String validUserToken;
    private String anotherUserToken;
    private UUID userId;
    private UUID anotherUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        validUserToken = jwtService.generateToken(userId);
        anotherUserToken = jwtService.generateToken(anotherUserId);
    }

    @Test
    @DisplayName("Should generate valid JWT token via /auth/anonymous endpoint")
    void testGenerateAnonymousToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/anonymous"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.userId").isString());
    }

    @Test
    @DisplayName("Should deny access to protected endpoint without token")
    void testUnauthorizedAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with invalid Bearer token")
    void testInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should grant access with valid Bearer token")
    void testValidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow public access to /auth/anonymous endpoint")
    void testPublicAuthEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/anonymous"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User should only see their own jobs in list")
    void testGetJobsFiltersJobsByUserId() throws Exception {
        Job userJob = Job.builder()
                .userId(userId)
                .text("user's job")
                .status(JobStatus.CREATED)
                .fileName("user-file.txt")
                .build();
        jobRepository.save(userJob);

        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("User should not be able to access another user's job")
    void testGetJobByIdBlocksAccessToOtherUserJobs() throws Exception {
        Job anotherUserJob = Job.builder()
                .userId(anotherUserId)
                .text("another user's job")
                .status(JobStatus.CREATED)
                .fileName("other-file.txt")
                .build();
        Job savedJob = jobRepository.save(anotherUserJob);

        mockMvc.perform(get("/api/v1/jobs/" + savedJob.getId())
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("User should be able to access their own job by ID")
    void testGetJobByIdAllowsAccessToOwnJob() throws Exception {
        Job userJob = Job.builder()
                .userId(userId)
                .text("my job")
                .status(JobStatus.CREATED)
                .fileName("my-file.txt")
                .build();
        Job savedJob = jobRepository.save(userJob);

        mockMvc.perform(get("/api/v1/jobs/" + savedJob.getId())
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.sourceFile").doesNotExist())
                .andExpect(jsonPath("$.resultFile").doesNotExist());
    }

    @Test
    @DisplayName("User should not be able to delete another user's job")
    void testDeleteJobBlocksAccessToOtherUserJobs() throws Exception {
        Job anotherUserJob = Job.builder()
                .userId(anotherUserId)
                .text("another user's job")
                .status(JobStatus.CREATED)
                .fileName("other-file.txt")
                .build();
        Job savedJob = jobRepository.save(anotherUserJob);

        mockMvc.perform(delete("/api/v1/jobs/" + savedJob.getId())
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());

        assertTrue(jobRepository.findById(savedJob.getId()).isPresent());
    }

    @Test
    @DisplayName("User should be able to delete their own job")
    void testDeleteJobAllowsDeleteOfOwnJob() throws Exception {
        Job userJob = Job.builder()
                .userId(userId)
                .text("my job")
                .status(JobStatus.CREATED)
                .fileName("my-file.txt")
                .build();
        Job savedJob = jobRepository.save(userJob);

        mockMvc.perform(delete("/api/v1/jobs/" + savedJob.getId())
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk());

        assertTrue(jobRepository.findById(savedJob.getId()).isEmpty());
    }

    @Test
    @DisplayName("User should not be able to push another user's job")
    void testPushJobBlocksAccessToOtherUserJobs() throws Exception {
        Job anotherUserJob = Job.builder()
                .userId(anotherUserId)
                .text("another user's job")
                .status(JobStatus.CREATED)
                .fileName("other-file.txt")
                .build();
        Job savedJob = jobRepository.save(anotherUserJob);

        mockMvc.perform(post("/api/v1/jobs/" + savedJob.getId() + "/push")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Each request should maintain separate user context")
    void testUserContextIsolation() throws Exception {
        Job job1 = Job.builder()
                .userId(userId)
                .text("job1")
                .status(JobStatus.CREATED)
                .fileName("file1.txt")
                .build();
        Job job2 = Job.builder()
                .userId(anotherUserId)
                .text("job2")
                .status(JobStatus.CREATED)
                .fileName("file2.txt")
                .build();

        jobRepository.save(job1);
        jobRepository.save(job2);

        mockMvc.perform(get("/api/v1/jobs")
                .header("Authorization", "Bearer " + anotherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}

