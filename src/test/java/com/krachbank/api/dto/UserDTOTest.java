package com.krachbank.api.dto;

import com.krachbank.api.models.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testToModel_AllFieldsSet() {
        UserDTO dto = new UserDTO(
                1L,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                LocalDateTime.now(),
                true,
                true,
                "John",
                "Doe",
                "john.doe@example.com",
                "1234567890",
                "123456789",
                "password123",
                "johndoe",
                true);

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