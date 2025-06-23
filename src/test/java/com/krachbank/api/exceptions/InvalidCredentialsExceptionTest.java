package com.krachbank.api.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



class InvalidCredentialsExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Invalid username or password";
        InvalidCredentialsException exception = new InvalidCredentialsException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InvalidCredentialsException exception = new InvalidCredentialsException("msg");
        assertTrue(exception instanceof RuntimeException);
    }
}