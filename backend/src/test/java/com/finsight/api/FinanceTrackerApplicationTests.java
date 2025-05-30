package com.finsight.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinanceTrackerApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the application context can be loaded successfully
        // It's a smoke test to catch major configuration issues
    }
} 