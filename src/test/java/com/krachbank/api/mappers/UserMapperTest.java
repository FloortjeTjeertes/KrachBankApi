package com.krachbank.api.mappers;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void testToModel_AllFields() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setDailyLimit(BigDecimal.valueOf(1000.0));
        LocalDateTime now = LocalDateTime.now();
        dto.setCreatedAt(now);
        dto.setVerified(true);
        dto.setActive(true);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPhoneNumber("1234567890");
        dto.setBSN(123456789);
        dto.setIsAdmin(true);

        User user = userMapper.toModel(dto);

        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getDailyLimit(), user.getDailyLimit());
        assertEquals(dto.getCreatedAt(), user.getCreatedAt());
        assertEquals(dto.isVerified(), user.isVerified());
        assertEquals(dto.isActive(), user.isActive());
        assertEquals(dto.getFirstName(), user.getFirstName());
        assertEquals(dto.getLastName(), user.getLastName());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getPhoneNumber(), user.getPhoneNumber());
        assertEquals(dto.getBSN(), user.getBSN());
        assertTrue(user.isAdmin());
    }

    @Test
    void testToModel_AdminNullDefaultsToFalse() {
        UserDTO dto = new UserDTO();
        dto.setIsAdmin(null);

        User user = userMapper.toModel(dto);

        assertFalse(user.isAdmin());
    }

    @Test
    void testToResponse_AllFields() {
        User user = new User();
        user.setId(2L);
        user.setUsername("jane_doe");
        user.setEmail("jane@example.com");

        UserDTO dto = userMapper.toResponse(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getEmail(), dto.getEmail());
    }

    @Test
    void testToResponse_NullModelThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userMapper.toResponse(null));
    }
}