package com.monotonic.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class MonotonicStackDemoApplicationTests {

    @Test
    @DisplayName("Spring Boot application context loads successfully with H2 database")
    void contextLoads() {
    }
}
