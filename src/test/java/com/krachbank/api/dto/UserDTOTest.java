package com.krachbank.api.dto;

import com.krachbank.api.models.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testToModel_AllFieldsSet() {

        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setDailyLimit(new BigDecimal("1000.00"));
        dto.setTransferLimit(new BigDecimal("500.00"));
        dto.setCreatedAt(LocalDateTime.now());
        dto.setVerified(true);
        dto.setActive(true);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhoneNumber("1234567890");
        dto.setBSN("123456789");
        dto.setPassword("password123");
        dto.setUsername("johndoe");
        dto.setIsAdmin(true);
        User user = dto.ToModel();

        assertEquals(dto.getId(), user.getId());
        assertEquals(BigDecimal.ZERO, user.getDailyLimit());
        assertEquals(BigDecimal.ZERO, user.getTransferLimit());
        assertEquals(dto.getCreatedAt(), user.getCreatedAt());
        assertEquals(dto.isVerified(), user.isVerified());
        assertEquals(dto.isActive(), user.isActive());
        assertEquals(dto.getFirstName(), user.getFirstName());
        assertEquals(dto.getLastName(), user.getLastName());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getPhoneNumber(), user.getPhoneNumber());
        assertEquals(dto.getBSN(), user.getBSN());
        assertEquals(dto.getUsername(), user.getUsername());
        assertEquals(dto.getIsAdmin(), user.isAdmin());
    }

    @Test
    void testToModel_NullLimits() {
        UserDTO dto = new UserDTO();
        dto.setDailyLimit(null);
        dto.setTransferLimit(null);
        

        User user = dto.ToModel();

        assertNull(user.getDailyLimit());
        assertNull(user.getTransferLimit());
    }

    @Test
    void testToModel_DefaultValues() {
        UserDTO dto = new UserDTO();
        User user = dto.ToModel();

        assertNull(user.getId());
        assertNull(user.getDailyLimit());
        assertNull(user.getTransferLimit());
        assertNull(user.getCreatedAt());
        assertFalse(user.isVerified());
        assertFalse(user.isActive());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertNull(user.getPhoneNumber());
        assertEquals(0, user.getBSN());
        assertNull(user.getUsername());
        assertNull(user.isAdmin());
    }
}