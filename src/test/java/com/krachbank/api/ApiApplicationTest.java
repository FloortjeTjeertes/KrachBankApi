package com.krachbank.api;
        
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;




@SpringBootTest
class ApiApplicationTest {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
    }

    @Test
    void mainMethodRunsWithoutException() {
        assertDoesNotThrow(() -> ApiApplication.main(new String[]{}));
    }
}