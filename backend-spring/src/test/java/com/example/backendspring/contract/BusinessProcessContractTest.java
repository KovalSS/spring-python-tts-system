package com.example.backendspring.contract;

import com.example.backendspring.entity.Job;
import com.example.backendspring.entity.JobStatus;
import com.example.backendspring.integration.BaseIntegrationTest;
import com.example.backendspring.repository.JobRepository;
import com.example.backendspring.service.JwtService;
import com.example.backendspring.support.TestResources;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@ActiveProfiles("test")
@Transactional
class BusinessProcessContractTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JobRepository jobRepository;

    private MockMvc mockMvc;
    private String userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        userToken = jwtService.generateToken(TestResources.USER_ID);
    }

    @Test
    void listJobsReturnsSanitizedDtoContract() throws Exception {
        Job job = TestResources.job(TestResources.USER_ID, JobStatus.CREATED, "contract-list.txt");
        jobRepository.save(job);

        mockMvc.perform(get("/api/v1/jobs")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").isString())
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].fileName").value("contract-list.txt"))
                .andExpect(jsonPath("$[0].sourceFile").doesNotExist())
                .andExpect(jsonPath("$[0].resultFile").doesNotExist())
                .andExpect(jsonPath("$[0].userId").doesNotExist());
    }

    @Test
    void getJobByIdReturnsSanitizedDtoContract() throws Exception {
        Job job = TestResources.job(TestResources.USER_ID, JobStatus.DONE, "contract-detail.txt");
        job.setResultFile("internal/result-path.mp3");
        Job savedJob = jobRepository.save(job);

        mockMvc.perform(get("/api/v1/jobs/" + savedJob.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedJob.getId().toString()))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.fileName").value("contract-detail.txt"))
                .andExpect(jsonPath("$.sourceFile").doesNotExist())
                .andExpect(jsonPath("$.resultFile").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist());
    }
}

