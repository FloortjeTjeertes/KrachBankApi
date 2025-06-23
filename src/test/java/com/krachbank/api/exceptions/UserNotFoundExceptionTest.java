package com.krachbank.api.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;




class UserNotFoundExceptionTest {

    @Test
    void testExceptionMessageIsSet() {
        String message = "User not found";
        UserNotFoundException exception = new UserNotFoundException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionIsInstanceOfRuntimeException() {
        UserNotFoundException exception = new UserNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}