package com.example.backendspring;

import com.example.backendspring.integration.PostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests extends PostgresContainerSupport {

    @Test
    void contextLoads() {

    }
}