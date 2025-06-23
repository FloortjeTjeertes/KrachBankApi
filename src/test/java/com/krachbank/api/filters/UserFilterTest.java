package com.krachbank.api.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;



class UserFilterTest {

    @Test
    void testDefaultValues() {
        UserFilter filter = new UserFilter();
        assertEquals(100, filter.getLimit());
        assertEquals(0, filter.getOffset());
        assertNull(filter.getEmail());
        assertNull(filter.getCreatedBefore());
        assertNull(filter.getCreatedAfter());
        assertNull(filter.getIsVerified());
        assertNull(filter.getIsActive());
        assertNull(filter.getRole());
        assertNull(filter.getUseeName());
        assertNull(filter.getLastName());
    }

    @Test
    void testSetAndGetEmail() {
        UserFilter filter = new UserFilter();
        filter.setEmail("test@example.com");
        assertEquals("test@example.com", filter.getEmail());
    }

    @Test
    void testSetAndGetCreatedBefore() {
        UserFilter filter = new UserFilter();
        filter.setCreatedBefore("2024-01-01");
        assertEquals("2024-01-01", filter.getCreatedBefore());
    }

    @Test
    void testSetAndGetCreatedAfter() {
        UserFilter filter = new UserFilter();
        filter.setCreatedAfter("2023-01-01");
        assertEquals("2023-01-01", filter.getCreatedAfter());
    }

    @Test
    void testSetAndGetIsVerified() {
        UserFilter filter = new UserFilter();
        filter.setIsVerified("true");
        assertEquals("true", filter.getIsVerified());
    }

    @Test
    void testSetAndGetIsActive() {
        UserFilter filter = new UserFilter();
        filter.setIsActive("false");
        assertEquals("false", filter.getIsActive());
    }

    @Test
    void testSetAndGetRole() {
        UserFilter filter = new UserFilter();
        filter.setRole("ADMIN");
        assertEquals("ADMIN", filter.getRole());
    }

    @Test
    void testSetAndGetUserName() {
        UserFilter filter = new UserFilter();
        filter.setUserName("John");
        // Note: getUseeName() is likely a typo in the original code
        assertEquals("John", filter.getUseeName());
    }

    @Test
    void testSetAndGetLastName() {
        UserFilter filter = new UserFilter();
        filter.setLastName("Doe");
        assertEquals("Doe", filter.getLastName());
    }

    @Test
    void testSetAndGetLimit() {
        UserFilter filter = new UserFilter();
        filter.setLimit(50);
        assertEquals(50, filter.getLimit());
    }

    @Test
    void testSetAndGetOffset() {
        UserFilter filter = new UserFilter();
        filter.setOffset(10);
        assertEquals(10, filter.getOffset());
    }
}