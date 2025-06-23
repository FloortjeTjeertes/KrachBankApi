package com.krachbank.api.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



class UserAlreadyExistsExceptionTest {

    @Test
    void testExceptionMessageIsSet() {
        String message = "User already exists";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionIsInstanceOfRuntimeException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("msg");
        assertTrue(exception instanceof RuntimeException);
    }
}